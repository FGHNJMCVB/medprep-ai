import React from "react";
import { useEffect, useRef, useState } from "react";

import {
  login,
  register,
  getSubjects,
  getTopicsBySubject,
  getMockConfig,
  getCurrentMockTest,
  startMockTest,
  submitAnswer,
  submitMockTest,
  getMockResult,
  getMockHistory,
  startPractice,
  submitPracticeAnswer,
  finishPractice,
  getPracticeResult,
  getPracticeReview,
  getPracticeHistory
} from "./api";

function App() {
  const MOCK_STORAGE_KEY = "medprep_active_mock";

  const PRACTICE_QUESTION_COUNTS = [
    10,
    20,
    30,
    50,
    100,
    150
  ];

  // ==========================================================
  // AUTH
  // ==========================================================

  const [token, setToken] = useState(
    () => localStorage.getItem("studentToken")
  );

  const [screen, setScreen] = useState(
    () =>
      localStorage.getItem("studentToken")
        ? "dashboard"
        : "login"
  );

  const [authMode, setAuthMode] = useState("login");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [confirmPassword, setConfirmPassword] =
    useState("");

  // ==========================================================
  // COMMON
  // ==========================================================

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // ==========================================================
  // DASHBOARD
  // ==========================================================

  const [subjects, setSubjects] = useState([]);

  const [selectedSubject, setSelectedSubject] =
    useState(null);

  const [topics, setTopics] = useState([]);

  const [topicsLoading, setTopicsLoading] =
    useState(false);

  // ==========================================================
  // MOCK CONFIG
  // ==========================================================

  const [mockConfig, setMockConfig] = useState(null);

  // ==========================================================
  // MOCK SESSION
  // ==========================================================

  const [session, setSession] = useState(() => {
    try {
      const saved = JSON.parse(
        localStorage.getItem(MOCK_STORAGE_KEY) || "null"
      );
      return saved?.session || null;
    } catch {
      return null;
    }
  });

  const [currentIndex, setCurrentIndex] =
    useState(() => {
      try {
        const saved = JSON.parse(
          localStorage.getItem(MOCK_STORAGE_KEY) || "null"
        );
        return Number(saved?.currentIndex) || 0;
      } catch {
        return 0;
      }
    });

  const [selectedOptionId, setSelectedOptionId] =
    useState(null);

  // Stores the selected answer for every mock question.
  // Key = sessionQuestionId, value = selected option id.
  const [mockAnswers, setMockAnswers] =
    useState(() => {
      try {
        const saved = JSON.parse(
          localStorage.getItem(MOCK_STORAGE_KEY) || "null"
        );
        return saved?.mockAnswers && typeof saved.mockAnswers === "object"
          ? saved.mockAnswers
          : {};
      } catch {
        return {};
      }
    });

  // Tracks answers that have already been persisted
  // to the backend during final submission.
  const [submittedMockQuestionIds, setSubmittedMockQuestionIds] =
    useState(() => new Set());

  const [answerStartTime, setAnswerStartTime] =
    useState(Date.now());

  // Remaining mock-exam time in seconds.
  const [mockRemainingSeconds, setMockRemainingSeconds] =
    useState(() => {
      try {
        const saved = JSON.parse(
          localStorage.getItem(MOCK_STORAGE_KEY) || "null"
        );
        const remaining = Number(saved?.mockRemainingSeconds) || 0;
        const savedAt = Number(saved?.savedAt) || Date.now();
        const elapsed = Math.max(0, Math.floor((Date.now() - savedAt) / 1000));
        return Math.max(0, remaining - elapsed);
      } catch {
        return 0;
      }
    });

  // FMGE has two parts: 150 questions and 150 minutes each.
  const [mockPart, setMockPart] = useState(() => {
    try {
      const saved = JSON.parse(
        localStorage.getItem(MOCK_STORAGE_KEY) || "null"
      );
      return saved?.mockPart === 2 ? 2 : 1;
    } catch {
      return 1;
    }
  });

  const [showPartTransition, setShowPartTransition] =
    useState(() => {
      try {
        const saved = JSON.parse(
          localStorage.getItem(MOCK_STORAGE_KEY) || "null"
        );
        return Boolean(saved?.showPartTransition);
      } catch {
        return false;
      }
    });

  // Keeps the latest finishExam function available to the timer
  // without resetting the timer whenever answers change.
  const finishExamRef = useRef(null);

  // ==========================================================
  // PRACTICE SESSION
  // ==========================================================

  const [practiceSession, setPracticeSession] =
    useState(null);

  const [
    practiceCurrentIndex,
    setPracticeCurrentIndex
  ] = useState(0);

  const [
    practiceSelectedOptionId,
    setPracticeSelectedOptionId
  ] = useState(null);

  const [
    practiceAnswerStartTime,
    setPracticeAnswerStartTime
  ] = useState(Date.now());

  const [
    practiceResult,
    setPracticeResult
  ] = useState(null);

  const [
    practiceReview,
    setPracticeReview
  ] = useState(null);

  // Number of questions selected for the next practice session.
  const [practiceQuestionCount, setPracticeQuestionCount] =
    useState(10);

  // ==========================================================
  // RESULT / HISTORY
  // ==========================================================

  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);

  const [practiceHistory, setPracticeHistory] =
    useState([]);

  // ==========================================================
  // PERSIST ACTIVE MOCK
  // ==========================================================

  useEffect(() => {
    if (!session || session.status === "COMPLETED") {
      localStorage.removeItem(MOCK_STORAGE_KEY);
      return;
    }

    try {
      const existing = JSON.parse(
        localStorage.getItem(MOCK_STORAGE_KEY) || "null"
      );

      localStorage.setItem(
        MOCK_STORAGE_KEY,
        JSON.stringify({
          session,
          currentIndex,
          mockAnswers,
          submittedMockQuestionIds: Array.from(submittedMockQuestionIds),
          mockRemainingSeconds,
          mockPart,
          showPartTransition,
          savedAt: Date.now(),
          token
        })
      );

      if (existing?.session?.sessionId !== session.sessionId) {
        setSelectedOptionId(
          mockAnswers[session.questions?.[currentIndex]?.sessionQuestionId] ?? null
        );
      }
    } catch (storageError) {
      console.warn("Unable to persist active mock test:", storageError);
    }
  }, [
    token,
    session,
    currentIndex,
    mockAnswers,
    submittedMockQuestionIds,
    mockRemainingSeconds,
    mockPart,
    showPartTransition
  ]);

  // ==========================================================
  // RESTORE ACTIVE MOCK ON APP START
  // ==========================================================

  useEffect(() => {
    if (!token || !session || !Array.isArray(session.questions)) {
      return;
    }

    const activeQuestion = session.questions[currentIndex];

    setSelectedOptionId(
      activeQuestion
        ? mockAnswers[activeQuestion.sessionQuestionId] ?? null
        : null
    );

    if (screen === "dashboard") {
      if (mockRemainingSeconds > 0 && !showPartTransition) {
        setScreen("exam");
      } else if (mockRemainingSeconds <= 0) {
        if (mockPart === 1) {
          setShowPartTransition(true);
          setScreen("exam");
        } else {
          Promise.resolve(
            finishExamRef.current?.()
          ).catch(restoreError => {
            console.error(
              "Failed to finish expired mock test:",
              restoreError
            );
          });
        }
      }
    }
  }, []);

  // ==========================================================
  // RESTORE ACTIVE MOCK FROM SERVER
  // ==========================================================
  //
  // Browser storage keeps the student's local progress. The server
  // is the source of truth for the active mock's questions/options.
  // This also allows recovery when browser storage is unavailable.
  //
  useEffect(() => {
    if (!token) {
      return;
    }

    let cancelled = false;

    async function restoreServerMock() {
      try {
        const serverSession = await getCurrentMockTest();

        if (
          cancelled ||
          !serverSession ||
          !Array.isArray(serverSession.questions) ||
          serverSession.questions.length === 0
        ) {
          return;
        }

        const savedRaw = localStorage.getItem(MOCK_STORAGE_KEY);
        let saved = null;

        try {
          saved = savedRaw ? JSON.parse(savedRaw) : null;
        } catch {
          saved = null;
        }

        // Merge fresh server questions/options with existing local progress.
        setSession(previous => {
          if (!previous) {
            return serverSession;
          }

          return {
            ...serverSession,
            ...previous,
            questions: serverSession.questions
          };
        });

        // If there is no local session, initialize a safe server-side resume.
        if (!saved?.session) {
          setCurrentIndex(0);
          setMockPart(1);
          setShowPartTransition(false);

          const partDurationSeconds =
            (Number(mockConfig?.partDurationMinutes) || 150) * 60;

          const startedAtMs = Date.parse(
            serverSession.startedAt || ""
          );

          const elapsedSeconds = Number.isFinite(startedAtMs)
            ? Math.max(
                0,
                Math.floor(
                  (Date.now() - startedAtMs) / 1000
                )
              )
            : 0;

          setMockRemainingSeconds(
            Math.max(
              0,
              partDurationSeconds - elapsedSeconds
            )
          );

          setScreen("exam");
        }
      } catch (resumeError) {
        // 404/no-active-session is expected for normal dashboard use.
        console.debug(
          "No active mock available on server:",
          resumeError
        );
      }
    }

    restoreServerMock();

    return () => {
      cancelled = true;
    };
  }, [token, mockConfig?.partDurationMinutes]);

  // ==========================================================
  // LOAD DASHBOARD
  // ==========================================================

  useEffect(() => {
    if (!token) {
      return;
    }

    let cancelled = false;

    async function loadDashboard() {
      try {
        setLoading(true);
        setError("");

        const [
          subjectsData,
          configData,
          historyData,
          practiceHistoryData
        ] = await Promise.all([
          getSubjects(),
          getMockConfig(),
          getMockHistory(),
          getPracticeHistory()
        ]);

        if (cancelled) {
          return;
        }

        setSubjects(
          Array.isArray(subjectsData)
            ? subjectsData
            : []
        );

        setMockConfig(configData);

        setHistory(
          Array.isArray(historyData)
            ? historyData
            : []
        );

        setPracticeHistory(
          Array.isArray(practiceHistoryData)
            ? practiceHistoryData
            : []
        );
      } catch (loadError) {
        if (!cancelled) {
          console.error(
            "Failed to load dashboard:",
            loadError
          );

          setError(
            loadError?.message ||
              "Unable to load dashboard."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadDashboard();

    return () => {
      cancelled = true;
    };
  }, [token]);

  // ==========================================================
  // LOGIN
  // ==========================================================

  async function handleLogin(event) {
    event.preventDefault();

    setLoading(true);
    setError("");

    try {
      const response = await login(
        email.trim(),
        password
      );

      const newToken =
        response?.token ??
        response?.accessToken ??
        response?.jwt;

      if (!newToken) {
        throw new Error(
          "Login succeeded, but no JWT token was returned."
        );
      }

      localStorage.setItem(
        "studentToken",
        newToken
      );

      setToken(newToken);
      setScreen("dashboard");
      setPassword("");
    } catch (loginError) {
      console.error(
        "Login failed:",
        loginError
      );

      setError(
        loginError?.message ||
          "Login failed."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // REGISTER
  // ==========================================================

  async function handleRegister(event) {
    event.preventDefault();

    setLoading(true);
    setError("");

    if (!name.trim()) {
      setError("Name is required.");
      setLoading(false);
      return;
    }

    if (!email.trim()) {
      setError("Email is required.");
      setLoading(false);
      return;
    }

    if (!password) {
      setError("Password is required.");
      setLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setError(
        "Password and confirm password do not match."
      );
      setLoading(false);
      return;
    }

    try {
      /*
       * api.js register() expects:
       * register(firstName, lastName, email, password)
       *
       * This UI currently has one name field.
       * So use the entered name as firstName.
       */
      await register(
        name.trim(),
        "",
        email.trim(),
        password
      );

      setAuthMode("login");
      setPassword("");
      setConfirmPassword("");

      setError(
        "Account created successfully. Please sign in."
      );
    } catch (registerError) {
      console.error(
        "Registration failed:",
        registerError
      );

      setError(
        registerError?.message ||
          "Unable to create account."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // AUTH MODE
  // ==========================================================

  function switchToRegister() {
    setAuthMode("register");
    setError("");
    setPassword("");
    setConfirmPassword("");
  }

  function switchToLogin() {
    setAuthMode("login");
    setError("");
    setPassword("");
    setConfirmPassword("");
    setName("");
  }

  // ==========================================================
  // LOGOUT
  // ==========================================================

  function handleLogout() {
    localStorage.removeItem(
      "studentToken"
    );
    localStorage.removeItem(MOCK_STORAGE_KEY);

    setToken(null);

    setSession(null);
    setPracticeSession(null);

    setResult(null);
    setPracticeResult(null);
    setPracticeReview(null);

    setHistory([]);
    setPracticeHistory([]);

    setSubjects([]);
    setSelectedSubject(null);
    setTopics([]);

    setMockConfig(null);

    setCurrentIndex(0);
    setSelectedOptionId(null);
    setMockPart(1);
    setShowPartTransition(false);
    setMockRemainingSeconds(0);
    setMockAnswers({});
    setSubmittedMockQuestionIds(new Set());

    setPracticeCurrentIndex(0);
    setPracticeSelectedOptionId(null);
    setPracticeQuestionCount(10);

    setEmail("");
    setPassword("");
    setName("");
    setConfirmPassword("");

    setError("");
    setAuthMode("login");
    setScreen("login");
  }

  // ==========================================================
  // SELECT SUBJECT
  // ==========================================================

  async function handleSubjectSelect(subject) {
    setSelectedSubject(subject);
    setTopics([]);
    setError("");
    setTopicsLoading(true);

    try {
      const data =
        await getTopicsBySubject(
          subject.id
        );

      setTopics(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (topicError) {
      console.error(
        "Failed to load topics:",
        topicError
      );

      setError(
        topicError?.message ||
          "Unable to load topics."
      );
    } finally {
      setTopicsLoading(false);
    }
  }

  // ==========================================================
  // BACK TO SUBJECTS
  // ==========================================================

  function handleBackToSubjects() {
    setSelectedSubject(null);
    setTopics([]);
    setError("");
  }

  // ==========================================================
  // START PRACTICE
  // ==========================================================

  async function handleStartPractice(topic) {
    setLoading(true);
    setError("");
    setPracticeResult(null);
    setPracticeReview(null);

    try {
      const practice =
        await startPractice({
          topicId: topic.id,
          numberOfQuestions: practiceQuestionCount
        });

      if (
        !practice ||
        !Array.isArray(
          practice.questions
        ) ||
        practice.questions.length === 0
      ) {
        throw new Error(
          "Practice session was created, but no questions were returned."
        );
      }

      setPracticeSession(practice);

      setPracticeCurrentIndex(0);

      setPracticeSelectedOptionId(
        null
      );

      setPracticeAnswerStartTime(
        Date.now()
      );

      setScreen("practice");
    } catch (practiceError) {
      console.error(
        "Failed to start practice:",
        practiceError
      );

      setError(
        practiceError?.message ||
          "Unable to start practice."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // PRACTICE SELECT OPTION
  // ==========================================================

  function handlePracticeOptionSelect(
    optionId
  ) {
    setPracticeSelectedOptionId(
      optionId
    );

    setError("");
  }

  // ==========================================================
  // FINISH PRACTICE
  // ==========================================================

  async function finishPracticeSession() {
    if (!practiceSession) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      await finishPractice(
        practiceSession.sessionId
      );

      const finalResult =
        await getPracticeResult(
          practiceSession.sessionId
        );

      setPracticeResult(
        finalResult
      );

      setScreen(
        "practice-result"
      );
    } catch (finishError) {
      console.error(
        "Failed to finish practice:",
        finishError
      );

      setError(
        finishError?.message ||
          "Unable to finish practice."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // PRACTICE NEXT
  // ==========================================================

  async function handlePracticeNext() {
    if (
      !practiceSession ||
      !Array.isArray(
        practiceSession.questions
      ) ||
      !practiceSession.questions[
        practiceCurrentIndex
      ]
    ) {
      setError(
        "Current practice question is unavailable."
      );
      return;
    }

    if (
      practiceSelectedOptionId === null
    ) {
      setError(
        "Please select an answer first."
      );
      return;
    }

    const currentQuestion =
      practiceSession.questions[
        practiceCurrentIndex
      ];

    const timeTakenSeconds =
      Math.max(
        0,
        Math.floor(
          (Date.now() -
            practiceAnswerStartTime) /
            1000
        )
      );

    setLoading(true);
    setError("");

    try {
      const answer =
        await submitPracticeAnswer(
          practiceSession.sessionId,
          currentQuestion.sessionQuestionId,
          practiceSelectedOptionId,
          timeTakenSeconds
        );

      setPracticeSession(
        previous => ({
          ...previous,

          answeredQuestions:
            answer?.answeredQuestions ??
            previous.answeredQuestions,

          correctAnswers:
            answer?.correctAnswers ??
            previous.correctAnswers
        })
      );

      const nextIndex =
        practiceCurrentIndex + 1;

      if (
        nextIndex >=
        practiceSession.questions.length
      ) {
        await finishPracticeSession();
        return;
      }

      setPracticeCurrentIndex(
        nextIndex
      );

      setPracticeSelectedOptionId(
        null
      );

      setPracticeAnswerStartTime(
        Date.now()
      );
    } catch (answerError) {
      console.error(
        "Failed to submit practice answer:",
        answerError
      );

      setError(
        answerError?.message ||
          "Unable to submit practice answer."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // PRACTICE REVIEW
  // ==========================================================

  async function handlePracticeReview() {

    if (!practiceSession?.sessionId) {
      setError(
        "Practice session is unavailable."
      );
      return;
    }

    setLoading(true);
    setError("");

    try {

      const review =
        await getPracticeReview(
          practiceSession.sessionId
        );

      if (
        !review ||
        !Array.isArray(
          review.questions
        )
      ) {
        throw new Error(
          "Practice review data is unavailable."
        );
      }

      setPracticeReview(review);
      setScreen("practice-review");

    } catch (reviewError) {

      console.error(
        "Failed to load practice review:",
        reviewError
      );

      setError(
        reviewError?.message ||
          "Unable to load practice review."
      );

    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // VIEW PRACTICE REVIEW FROM HISTORY
  // ==========================================================

  async function handleViewPracticeReview(sessionId) {
    if (!sessionId) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      const review =
        await getPracticeReview(sessionId);

      if (
        !review ||
        !Array.isArray(review.questions)
      ) {
        throw new Error(
          "Practice review data is unavailable."
        );
      }

      setPracticeReview(review);
      setPracticeSession(null);
      setPracticeResult(null);
      setScreen("practice-review");
    } catch (reviewError) {
      console.error(
        "Failed to load practice review:",
        reviewError
      );

      setError(
        reviewError?.message ||
          "Unable to load practice review."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // PRACTICE RESULT -> DASHBOARD
  // ==========================================================

  function handlePracticeResultDashboard() {
    setPracticeSession(null);
    setPracticeResult(null);
    setPracticeReview(null);
    setPracticeCurrentIndex(0);
    setPracticeSelectedOptionId(null);

    setSelectedSubject(null);
    setTopics([]);

    setError("");
    setScreen("dashboard");
  }

  // ==========================================================
  // START MOCK
  // ==========================================================

  async function handleStartMock() {
    if (!mockConfig?.id) {
      setError(
        "Mock test configuration is unavailable."
      );
      return;
    }

    setLoading(true);
    setError("");

    try {
      const mock =
        await startMockTest(
          mockConfig.id
        );

      const expectedTotalQuestions =
        Number(mockConfig.totalQuestions) || 300;

      if (
        !mock ||
        !Array.isArray(mock.questions) ||
        mock.questions.length !== expectedTotalQuestions
      ) {
        throw new Error(
          `FMGE mock test must contain exactly ${expectedTotalQuestions} questions, but ${mock?.questions?.length ?? 0} were returned.`
        );
      }

      setSession(mock);
      setCurrentIndex(0);
      setSelectedOptionId(null);
      setMockAnswers({});
      setSubmittedMockQuestionIds(new Set());
      setMockPart(1);
      setShowPartTransition(false);
      setAnswerStartTime(
        Date.now()
      );

      const durationMinutes =
        Number(mockConfig.partDurationMinutes) || 0;

      setMockRemainingSeconds(
        Math.max(0, durationMinutes * 60)
      );

      setScreen("exam");
    } catch (startError) {
      console.error(
        "Failed to start mock test:",
        startError
      );

      setError(
        startError?.message ||
          "Unable to start mock test."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // MOCK SELECT OPTION
  // ==========================================================

  function handleOptionSelect(
    optionId
  ) {
    if (!session) {
      return;
    }

    const currentQuestion =
      session.questions?.[currentIndex];

    if (!currentQuestion) {
      return;
    }

    setSelectedOptionId(
      optionId
    );

    setMockAnswers(
      previous => ({
        ...previous,
        [currentQuestion.sessionQuestionId]:
          optionId
      })
    );

    setError("");
  }

  // ==========================================================
  // MOCK QUESTION NAVIGATION
  // ==========================================================

  function getMockPartQuestionCount() {
    return Math.min(
      Number(mockConfig?.partQuestions) || 150,
      session?.questions?.length || 0
    );
  }

  function getMockPartStartIndex() {
    const partQuestions = getMockPartQuestionCount();
    return mockPart === 2 ? partQuestions : 0;
  }

  function getMockPartEndIndex() {
    const partQuestions = getMockPartQuestionCount();
    const total = session?.questions?.length || 0;

    return mockPart === 1
      ? Math.min(partQuestions, total) - 1
      : total - 1;
  }

  function startPartTwo() {
    if (!session || !Array.isArray(session.questions)) {
      return;
    }

    const partQuestions = getMockPartQuestionCount();

    if (session.questions.length <= partQuestions) {
      setError("Part 2 questions are unavailable.");
      return;
    }

    setMockPart(2);
    setShowPartTransition(false);
    setCurrentIndex(partQuestions);
    setSelectedOptionId(
      mockAnswers[session.questions[partQuestions].sessionQuestionId] ?? null
    );
    setMockRemainingSeconds(
      Math.max(0, (Number(mockConfig?.partDurationMinutes) || 150) * 60)
    );
    setAnswerStartTime(Date.now());
    setError("");
  }

  function goToMockQuestion(index) {
    if (
      !session ||
      !Array.isArray(session.questions)
    ) {
      return;
    }

    const startIndex = getMockPartStartIndex();
    const endIndex = getMockPartEndIndex();

    if (
      index < startIndex ||
      index > endIndex
    ) {
      return;
    }

    const targetQuestion =
      session.questions[index];

    setCurrentIndex(index);
    setSelectedOptionId(
      mockAnswers[targetQuestion.sessionQuestionId] ?? null
    );
    setAnswerStartTime(Date.now());
    setError("");
  }

  function handlePrevious() {
    const startIndex = getMockPartStartIndex();

    if (currentIndex <= startIndex) {
      return;
    }

    goToMockQuestion(currentIndex - 1);
  }

  function handleNext() {
    if (!session?.questions?.length) {
      setError("Mock test questions are unavailable.");
      return;
    }

    const endIndex = getMockPartEndIndex();

    if (currentIndex < endIndex) {
      goToMockQuestion(currentIndex + 1);
      return;
    }

    if (mockPart === 1) {
      setShowPartTransition(true);
      setSelectedOptionId(null);
      setMockRemainingSeconds(0);
      return;
    }

    return;
  }

  function handleClearAnswer() {
    if (!session) {
      return;
    }

    const currentQuestion =
      session.questions?.[currentIndex];

    if (!currentQuestion) {
      return;
    }

    const questionId =
      currentQuestion.sessionQuestionId;

    setMockAnswers(previous => {
      const next = { ...previous };
      delete next[questionId];
      return next;
    });

    setSelectedOptionId(null);
    setError("");
  }

  // ==========================================================
  // SUBMIT MOCK EXAM
  // ==========================================================

  async function finishExam() {
    if (!session) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      let answeredQuestions =
        session.answeredQuestions ?? 0;
      let correctAnswers =
        session.correctAnswers ?? 0;

      // Persist every selected answer exactly once.
      // Unanswered questions are intentionally skipped.
      for (const question of session.questions ?? []) {
        const questionId =
          question.sessionQuestionId;

        const optionId =
          mockAnswers[questionId];

        if (
          optionId == null ||
          submittedMockQuestionIds.has(questionId)
        ) {
          continue;
        }

        const answer =
          await submitAnswer(
            session.sessionId,
            questionId,
            optionId,
            0
          );

        answeredQuestions =
          answer?.answeredQuestions ??
          answeredQuestions + 1;

        correctAnswers =
          answer?.correctAnswers ??
          correctAnswers;

        setSubmittedMockQuestionIds(
          previous => {
            const next = new Set(
              previous
            );
            next.add(questionId);
            return next;
          }
        );
      }

      setSession(
        previous => ({
          ...previous,
          answeredQuestions,
          correctAnswers
        })
      );

      const submission =
        await submitMockTest(
          session.sessionId
        );

      setResult(
        submission
      );

      setScreen(
        "result"
      );

      try {
        const past =
          await getMockHistory();

        setHistory(
          Array.isArray(past)
            ? past
            : []
        );
      } catch (historyError) {
        console.warn(
          "Unable to refresh history:",
          historyError
        );
      }
    } catch (submitError) {
      console.error(
        "Failed to submit mock test:",
        submitError
      );

      setError(
        submitError?.message ||
          "Unable to submit mock test."
      );
    } finally {
      setLoading(false);
    }
  }

  // Keep the latest submit function available to the countdown timer.
  finishExamRef.current = finishExam;

  // ==========================================================
  // MOCK EXAM TIMER
  // ==========================================================

  useEffect(() => {
    if (
      screen !== "exam" ||
      !session ||
      showPartTransition ||
      mockRemainingSeconds <= 0
    ) {
      return;
    }

    const timerId = setInterval(() => {
      setMockRemainingSeconds(previous => {
        if (previous <= 1) {
          clearInterval(timerId);

          if (mockPart === 1) {
            setShowPartTransition(true);
            setSelectedOptionId(null);
            return 0;
          }

          Promise.resolve(
            finishExamRef.current?.()
          ).catch(timerError => {
            console.error(
              "Failed to auto-submit mock test:",
              timerError
            );
          });

          return 0;
        }

        return previous - 1;
      });
    }, 1000);

    return () => {
      clearInterval(timerId);
    };
  }, [
    screen,
    session?.sessionId,
    mockPart,
    showPartTransition,
    mockRemainingSeconds > 0
  ]);

  async function handleSubmitExam() {
    if (!session) {
      return;
    }

    const total =
      session.questions?.length ?? 0;

    const answered =
      Object.keys(mockAnswers).length;

    const unanswered =
      Math.max(
        0,
        total - answered
      );

    const confirmed =
      window.confirm(
        unanswered > 0
          ? `You have ${unanswered} unanswered question${unanswered === 1 ? "" : "s"}. Submit the exam anyway?`
          : "Submit your FMGE mock test? You will not be able to change your answers after submission."
      );

    if (!confirmed) {
      return;
    }

    await finishExam();
  }

  // ==========================================================
  // VIEW MOCK RESULT
  // ==========================================================

  async function handleViewResult(
    sessionId
  ) {
    setLoading(true);
    setError("");

    try {
      const data =
        await getMockResult(
          sessionId
        );

      setResult(data);
      setScreen("result");
    } catch (resultError) {
      console.error(
        "Failed to load result:",
        resultError
      );

      setError(
        resultError?.message ||
          "Unable to load result."
      );
    } finally {
      setLoading(false);
    }
  }

  // ==========================================================
  // LOGIN / REGISTER SCREEN
  // ==========================================================

  if (screen === "login") {
    const isRegister =
      authMode === "register";

    return (
      <div className="app-shell">
        <main className="auth-card">
          <div className="brand">
            <div className="brand-mark">
              M
            </div>

            <div>
              <h1>
                MedPrep AI
              </h1>

              <p>
                FMGE preparation platform
              </p>
            </div>
          </div>

          <h2>
            {isRegister
              ? "Create your account"
              : "Welcome back"}
          </h2>

          <p className="muted">
            {isRegister
              ? "Create an account to start your preparation."
              : "Sign in to continue your preparation."}
          </p>

          {error && (
            <div className="error-box">
              {error}
            </div>
          )}

          {isRegister ? (
            <form
              onSubmit={
                handleRegister
              }
            >
              <label htmlFor="name">
                Full name
              </label>

              <input
                id="name"
                type="text"
                value={name}
                onChange={
                  event =>
                    setName(
                      event.target.value
                    )
                }
                autoComplete="name"
                required
              />

              <label htmlFor="register-email">
                Email
              </label>

              <input
                id="register-email"
                type="email"
                value={email}
                onChange={
                  event =>
                    setEmail(
                      event.target.value
                    )
                }
                autoComplete="email"
                required
              />

              <label htmlFor="register-password">
                Password
              </label>

              <input
                id="register-password"
                type="password"
                value={password}
                onChange={
                  event =>
                    setPassword(
                      event.target.value
                    )
                }
                autoComplete="new-password"
                required
              />

              <label htmlFor="confirm-password">
                Confirm password
              </label>

              <input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={
                  event =>
                    setConfirmPassword(
                      event.target.value
                    )
                }
                autoComplete="new-password"
                required
              />

              <button
                type="submit"
                className="primary-button"
                disabled={loading}
              >
                {loading
                  ? "Creating account..."
                  : "Create account"}
              </button>
            </form>
          ) : (
            <form
              onSubmit={handleLogin}
            >
              <label htmlFor="email">
                Email
              </label>

              <input
                id="email"
                type="email"
                value={email}
                onChange={
                  event =>
                    setEmail(
                      event.target.value
                    )
                }
                autoComplete="email"
                required
              />

              <label htmlFor="password">
                Password
              </label>

              <input
                id="password"
                type="password"
                value={password}
                onChange={
                  event =>
                    setPassword(
                      event.target.value
                    )
                }
                autoComplete="current-password"
                required
              />

              <button
                type="submit"
                className="primary-button"
                disabled={loading}
              >
                {loading
                  ? "Signing in..."
                  : "Sign in"}
              </button>
            </form>
          )}

          <div className="auth-switch">
            {isRegister ? (
              <>
                Already have an account?

                <button
                  type="button"
                  onClick={
                    switchToLogin
                  }
                >
                  Sign in
                </button>
              </>
            ) : (
              <>
                Don't have an account?

                <button
                  type="button"
                  onClick={
                    switchToRegister
                  }
                >
                  Create account
                </button>
              </>
            )}
          </div>
        </main>
      </div>
    );
  }

  // ==========================================================
  // SUBJECT TOPICS
  // ==========================================================
  //
  // IMPORTANT:
  // This block MUST come before the Dashboard block.
  // Otherwise the Dashboard catches every "dashboard"
  // screen before React can reach the Topics screen.
  // ==========================================================

  if (
    screen === "dashboard" &&
    selectedSubject
  ) {
    return (
      <div className="app-shell">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark">
              M
            </div>

            <div>
              <strong>
                MedPrep AI
              </strong>

              <span>
                {selectedSubject.name}
              </span>
            </div>
          </div>

          <button
            className="ghost-button"
            onClick={
              handleLogout
            }
          >
            Logout
          </button>
        </header>

        <main className="dashboard">
          <button
            type="button"
            className="secondary-button"
            onClick={
              handleBackToSubjects
            }
          >
            ← Back to Subjects
          </button>

          <section className="topics-section">
            <div className="section-heading">
              <div>
                <p className="eyebrow">
                  {selectedSubject.name}
                </p>

                <h2>
                  Topics
                </h2>
              </div>

              <span>
                {topics.length} topics
              </span>
            </div>

            {error && (
              <div className="error-box">
                {error}
              </div>
            )}

            {topicsLoading ? (
              <div className="loading-card">
                Loading topics...
              </div>
            ) : topics.length === 0 ? (
              <div className="empty-card">
                <strong>
                  No topics available
                </strong>

                <p>
                  There are currently no
                  topics configured for this
                  subject.
                </p>
              </div>
            ) : (
              <>
                <div
                  className="empty-card"
                  style={{
                    marginBottom: "16px"
                  }}
                >
                  <strong>
                    Practice settings
                  </strong>

                  <p>
                    Choose how many questions you want
                    in this practice session.
                  </p>

                  <label
                    htmlFor="practice-question-count"
                  >
                    Number of questions
                  </label>

                  <select
                    id="practice-question-count"
                    value={practiceQuestionCount}
                    onChange={event =>
                      setPracticeQuestionCount(
                        Number(event.target.value)
                      )
                    }
                    disabled={loading}
                    style={{
                      display: "block",
                      marginTop: "8px",
                      minWidth: "180px"
                    }}
                  >
                    {PRACTICE_QUESTION_COUNTS.map(
                      count => (
                        <option
                          key={count}
                          value={count}
                        >
                          {count} Questions
                        </option>
                      )
                    )}
                  </select>
                </div>

                <div className="topic-list">
                {topics.map(
                  topic => (
                    <div
                      key={topic.id}
                      className="topic-row"
                    >
                      <div>
                        <strong>
                          {topic.name}
                        </strong>

                        <span>
                          Topic{" "}
                          {topic.displayOrder ??
                            ""}
                        </span>
                      </div>

                      <button
                        type="button"
                        className="primary-button"
                        onClick={() =>
                          handleStartPractice(
                            topic
                          )
                        }
                        disabled={
                          loading
                        }
                      >
                        Practice
                      </button>
                    </div>
                  )
                )}
                </div>
              </>
            )}
          </section>
        </main>
      </div>
    );
  }

  // ==========================================================
  // DASHBOARD
  // ==========================================================

  if (screen === "dashboard") {
    return (
      <div className="app-shell">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark">
              M
            </div>

            <div>
              <strong>
                MedPrep AI
              </strong>

              <span>
                FMGE
              </span>
            </div>
          </div>

          <button
            className="ghost-button"
            onClick={
              handleLogout
            }
          >
            Logout
          </button>
        </header>

        <main className="dashboard">
          {error && (
            <div className="error-box dashboard-error">
              {error}
            </div>
          )}

          <section className="hero">
            <div className="hero-content">
              <p className="eyebrow">
                {mockConfig?.name ||
                  "FMGE MOCK TEST"}
              </p>

              <h1>
                Prepare smarter.
              </h1>

              <p className="hero-text">
                Practice by subject and
                topic, or take a full
                FMGE mock test from your
                MedPrep question bank.
              </p>

              <button
                className="primary-button large"
                onClick={
                  handleStartMock
                }
                disabled={
                  loading ||
                  !mockConfig?.id
                }
              >
                {loading
                  ? "Preparing..."
                  : "Start Mock Test"}
              </button>
            </div>

            <div className="stats-card">
              <div className="stat-item">
                <span>
                  QUESTIONS
                </span>

                <strong>
                  {mockConfig?.totalQuestions ??
                    "—"}
                </strong>
              </div>

              <div className="stat-item">
                <span>
                  PART QUESTIONS
                </span>

                <strong>
                  {mockConfig?.partQuestions ??
                    "—"}
                </strong>
              </div>

              <div className="stat-item">
                <span>
                  DURATION
                </span>

                <strong>
                  {mockConfig?.partDurationMinutes
                    ? `${mockConfig.partDurationMinutes} min`
                    : "—"}
                </strong>
              </div>
            </div>
          </section>

          <section className="subjects-section">
            <div className="section-heading">
              <div>
                <p className="eyebrow">
                  STUDY
                </p>

                <h2>
                  Subjects
                </h2>
              </div>

              <span>
                {subjects.length} subjects
              </span>
            </div>

            {subjects.length === 0 ? (
              <div className="empty-card">
                No subjects available.
              </div>
            ) : (
              <div className="subject-grid">
                {subjects.map(
                  (subject, index) => (
                    <button
                      key={subject.id}
                      type="button"
                      className="subject-card"
                      onClick={() =>
                        handleSubjectSelect(
                          subject
                        )
                      }
                    >
                      <div className="subject-card-top">
                        <span className="subject-number">
                          {String(
                            index + 1
                          ).padStart(
                            2,
                            "0"
                          )}
                        </span>

                        <span className="subject-arrow">
                          →
                        </span>
                      </div>

                      <strong>
                        {subject.name}
                      </strong>

                      {subject.weightageMarks !=
                        null && (
                        <span>
                          {
                            subject.weightageMarks
                          }{" "}
                          marks
                        </span>
                      )}
                    </button>
                  )
                )}
              </div>
            )}
          </section>

          <section className="history-section">
            <div className="section-heading">
              <div>
                <p className="eyebrow">
                  PERFORMANCE
                </p>

                <h2>
                  Recent Practice
                </h2>
              </div>
            </div>

            {practiceHistory.length === 0 ? (
              <div className="empty-card">
                No completed practice sessions yet.
              </div>
            ) : (
              <div className="history-list">
                {practiceHistory.map(
                  item => (
                    <div
                      key={item.sessionId}
                      className="history-row"
                      style={{
                        cursor: "default"
                      }}
                    >
                      <div className="history-left">
                        <strong>
                          Practice Session #
                          {item.sessionId}
                        </strong>

                        <span>
                          {item.totalQuestions ??
                            0}{" "}
                          questions ·{" "}
                          {item.correctAnswers ??
                            0}{" "}
                          correct ·{" "}
                          {item.wrongAnswers ??
                            0}{" "}
                          wrong
                        </span>

                        <span>
                          {item.completedAt
                            ? new Date(
                                item.completedAt
                              ).toLocaleString()
                            : "Completed"}
                        </span>
                      </div>

                      <div className="history-score">
                        <strong>
                          {item.accuracy ??
                            0}
                          %
                        </strong>

                        <button
                          type="button"
                          className="secondary-button"
                          style={{
                            marginTop: "8px"
                          }}
                          onClick={() =>
                            handleViewPracticeReview(
                              item.sessionId
                            )
                          }
                          disabled={loading}
                        >
                          Review Answers
                        </button>
                      </div>
                    </div>
                  )
                )}
              </div>
            )}
          </section>

          <section className="history-section">
            <div className="section-heading">
              <div>
                <p className="eyebrow">
                  PERFORMANCE
                </p>

                <h2>
                  Recent mock tests
                </h2>
              </div>
            </div>

            {history.length === 0 ? (
              <div className="empty-card">
                No completed mock tests yet.
              </div>
            ) : (
              <div className="history-list">
                {history.map(
                  item => (
                    <button
                      key={
                        item.sessionId
                      }
                      className="history-row"
                      onClick={() =>
                        handleViewResult(
                          item.sessionId
                        )
                      }
                    >
                      <div className="history-left">
                        <strong>
                          Mock Test #
                          {
                            item.sessionId
                          }
                        </strong>

                        <span>
                          {item.completedAt
                            ? new Date(
                                item.completedAt
                              ).toLocaleString()
                            : "Completed"}
                        </span>
                      </div>

                      <div className="history-score">
                        <strong>
                          {item.percentage}%
                        </strong>

                        <span>
                          {item.passed
                            ? "Passed"
                            : "Not passed"}
                        </span>
                      </div>
                    </button>
                  )
                )}
              </div>
            )}
          </section>
        </main>
      </div>
    );
  }

  // ==========================================================
  // PRACTICE
  // ==========================================================

  if (screen === "practice") {
    if (
      !practiceSession ||
      !Array.isArray(
        practiceSession.questions
      ) ||
      !practiceSession.questions[
        practiceCurrentIndex
      ]
    ) {
      return (
        <div className="app-shell">
          <main className="loading-card">
            Loading practice...
          </main>
        </div>
      );
    }

    const currentQuestion =
      practiceSession.questions[
        practiceCurrentIndex
      ];

    const options =
      Array.isArray(
        currentQuestion.options
      )
        ? [
            ...currentQuestion.options
          ].sort(
            (a, b) =>
              (a.displayOrder ??
                0) -
              (b.displayOrder ??
                0)
          )
        : [];

    const total =
      practiceSession.questions.length;

    const progress =
      total > 0
        ? ((practiceCurrentIndex + 1) /
            total) *
          100
        : 0;

    return (
      <div className="exam-shell">
        <header className="exam-header">
          <div>
            <strong>
              {selectedSubject?.name ||
                "Practice"}
            </strong>

            <span>
              Practice Session #
              {
                practiceSession.sessionId
              }
            </span>
          </div>

          <div className="exam-counter">
            <strong>
              {practiceCurrentIndex + 1}
            </strong>

            <span>
              / {total}
            </span>
          </div>
        </header>

        <div className="progress-bar">
          <div
            style={{
              width: `${progress}%`
            }}
          />
        </div>

        <main className="question-area">
          <div className="question-card">
            <div className="question-topline">
              <span className="question-number">
                Question{" "}
                {practiceCurrentIndex + 1}
              </span>

              <span className="exam-counter">
                Answered{" "}
                {practiceSession.answeredQuestions ??
                  0}
                {" / "}
                {total}
              </span>
            </div>

            <h1 className="question-text">
              {
                currentQuestion.questionText
              }
            </h1>

            {error && (
              <div className="error-box">
                {error}
              </div>
            )}

            {options.length === 0 ? (
              <div className="error-box">
                This question has no options.
              </div>
            ) : (
              <div className="options-list">
                {options.map(
                  (option, index) => {
                    const selected =
                      practiceSelectedOptionId ===
                      option.id;

                    const label =
                      option.optionLabel ||
                      String.fromCharCode(
                        65 + index
                      );

                    return (
                      <button
                        key={
                          option.id
                        }
                        type="button"
                        className={
                          selected
                            ? "option-button selected"
                            : "option-button"
                        }
                        onClick={() =>
                          handlePracticeOptionSelect(
                            option.id
                          )
                        }
                        disabled={
                          loading
                        }
                      >
                        <span className="option-letter">
                          {label}
                        </span>

                        <span className="option-text">
                          {
                            option.optionText
                          }
                        </span>

                        <span className="option-radio">
                          {selected
                            ? "✓"
                            : ""}
                        </span>
                      </button>
                    );
                  }
                )}
              </div>
            )}

            <div className="question-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() =>
                  setPracticeSelectedOptionId(
                    null
                  )
                }
                disabled={loading}
              >
                Clear
              </button>

              <button
                type="button"
                className="primary-button"
                onClick={
                  handlePracticeNext
                }
                disabled={
                  loading ||
                  practiceSelectedOptionId ===
                    null
                }
              >
                {practiceCurrentIndex ===
                total - 1
                  ? "Finish Practice"
                  : "Next Question"}
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  // ==========================================================
  // PRACTICE RESULT
  // ==========================================================

  if (
    screen ===
    "practice-result"
  ) {
    const percentage =
      practiceResult?.accuracy ??
      0;

    return (
      <div className="app-shell">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark">
              M
            </div>

            <strong>
              MedPrep AI
            </strong>
          </div>

          <button
            className="ghost-button"
            onClick={
              handlePracticeResultDashboard
            }
          >
            Dashboard
          </button>
        </header>

        <main className="result-page">
          <section className="result-hero">
            <p className="eyebrow">
              PRACTICE RESULT
            </p>

            <h1>
              Practice complete.
            </h1>

            <div className="score-circle">
              <strong>
                {percentage}%
              </strong>

              <span>
                ACCURACY
              </span>
            </div>
          </section>

          <section className="result-grid">
            <div className="result-card">
              <span>
                Total
              </span>

              <strong>
                {practiceResult?.totalQuestions ??
                  0}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Answered
              </span>

              <strong>
                {practiceResult?.answeredQuestions ??
                  0}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Correct
              </span>

              <strong>
                {practiceResult?.correctAnswers ??
                  0}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Wrong
              </span>

              <strong>
                {practiceResult?.wrongAnswers ??
                  0}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Unanswered
              </span>

              <strong>
                {practiceResult?.unansweredQuestions ??
                  0}
              </strong>
            </div>
          </section>

          <div
            style={{
              display: "flex",
              justifyContent: "center",
              gap: "12px",
              flexWrap: "wrap",
              marginTop: "8px"
            }}
          >
            <button
              className="primary-button large"
              onClick={
                handlePracticeReview
              }
              disabled={loading}
            >
              {loading
                ? "Loading Review..."
                : "Review Answers"}
            </button>

            <button
              type="button"
              className="secondary-button large"
              onClick={
                handlePracticeResultDashboard
              }
            >
              Back to Dashboard
            </button>
          </div>
        </main>
      </div>
    );
  }

  // ==========================================================
  // PRACTICE REVIEW
  // ==========================================================

  if (screen === "practice-review") {

    if (
      !practiceReview ||
      !Array.isArray(
        practiceReview.questions
      )
    ) {
      return (
        <div className="app-shell">
          <main className="loading-card">
            Loading review...
          </main>
        </div>
      );
    }

    return (
      <div className="app-shell">

        <header className="topbar">

          <div className="brand">

            <div className="brand-mark">
              M
            </div>

            <div>
              <strong>
                MedPrep AI
              </strong>

              <span>
                Practice Review
              </span>
            </div>

          </div>

          <button
            type="button"
            className="ghost-button"
            onClick={
              handlePracticeResultDashboard
            }
          >
            Dashboard
          </button>

        </header>

        <main
          className="result-page"
          style={{
            textAlign: "left"
          }}
        >

          <section
            className="result-hero"
            style={{
              textAlign: "center"
            }}
          >

            <p className="eyebrow">
              PRACTICE REVIEW
            </p>

            <h1>
              Review your answers.
            </h1>

            <p className="muted">
              Review your selected answer,
              correct answer, explanation,
              and time taken.
            </p>

          </section>

          {practiceReview.questions.map(
            (item, index) => {

              const isCorrect =
                item.correct === true;

              const isUnanswered =
                item.selectedOptionId == null;

              return (
                <section
                  key={
                    item.sessionQuestionId ??
                    item.questionId ??
                    index
                  }
                  className="result-card"
                  style={{
                    marginBottom: "18px",
                    padding: "24px",
                    textAlign: "left"
                  }}
                >

                  <div
                    style={{
                      display: "flex",
                      justifyContent:
                        "space-between",
                      alignItems: "center",
                      gap: "16px",
                      flexWrap: "wrap",
                      marginBottom: "16px"
                    }}
                  >

                    <strong>
                      Question {index + 1}
                    </strong>

                    <strong>
                      {isUnanswered
                        ? "Unanswered"
                        : isCorrect
                          ? "✓ Correct"
                          : "✗ Incorrect"}
                    </strong>

                  </div>

                  <h2
                    style={{
                      marginTop: 0,
                      lineHeight: 1.5
                    }}
                  >
                    {item.questionText}
                  </h2>

                  <div
                    style={{
                      display: "grid",
                      gap: "10px",
                      marginTop: "18px"
                    }}
                  >

                    {(Array.isArray(
                      item.options
                    )
                      ? item.options
                      : []
                    ).map(
                      (option, optionIndex) => {

                        const isSelected =
                          option.id ===
                          item.selectedOptionId;

                        const isAnswer =
                          option.id ===
                          item.correctOptionId;

                        let background =
                          "#fafbfc";

                        if (isAnswer) {
                          background =
                            "#eef8f1";
                        } else if (isSelected) {
                          background =
                            "#fff4f4";
                        }

                        return (
                          <div
                            key={
                              option.id ??
                              optionIndex
                            }
                            style={{
                              padding:
                                "14px 16px",
                              borderRadius:
                                "12px",
                              border:
                                "1px solid #dfe5ec",
                              background
                            }}
                          >

                            <strong
                              style={{
                                marginRight:
                                  "8px"
                              }}
                            >
                              {option.optionLabel ||
                                String.fromCharCode(
                                  65 +
                                    optionIndex
                                )}
                            </strong>

                            <span>
                              {option.optionText}
                            </span>

                            {isAnswer && (
                              <span
                                style={{
                                  marginLeft:
                                    "10px",
                                  fontWeight: 700
                                }}
                              >
                                Correct answer
                              </span>
                            )}

                            {isSelected &&
                              !isAnswer && (
                                <span
                                  style={{
                                    marginLeft:
                                      "10px",
                                    fontWeight: 700
                                  }}
                                >
                                  Your answer
                                </span>
                              )}

                          </div>
                        );
                      }
                    )}

                  </div>

                  <div
                    className="notice-box"
                    style={{
                      marginTop: "18px"
                    }}
                  >

                    <strong>
                      Your answer:
                    </strong>{" "}
                    {item.selectedOptionText ||
                      "Not answered"}

                    <br />

                    <strong>
                      Correct answer:
                    </strong>{" "}
                    {item.correctOptionText ||
                      "Not available"}

                  </div>

                  <div
                    style={{
                      marginTop: "18px"
                    }}
                  >

                    <strong>
                      Explanation
                    </strong>

                    <p
                      style={{
                        lineHeight: 1.7,
                        marginBottom: 0
                      }}
                    >
                      {item.explanation ||
                        "No explanation is available for this question."}
                    </p>

                  </div>

                  {item.timeTakenSeconds !=
                    null && (
                    <div
                      className="muted"
                      style={{
                        marginTop: "12px"
                      }}
                    >
                      Time taken:{" "}
                      {item.timeTakenSeconds} seconds
                    </div>
                  )}

                </section>
              );
            }
          )}

          <div
            style={{
              display: "flex",
              justifyContent: "center",
              gap: "12px",
              flexWrap: "wrap",
              marginTop: "8px"
            }}
          >

            {practiceResult && (
              <button
                type="button"
                className="secondary-button large"
                onClick={() =>
                  setScreen(
                    "practice-result"
                  )
                }
              >
                ← Back to Result
              </button>
            )}

            <button
              type="button"
              className="primary-button large"
              onClick={
                handlePracticeResultDashboard
              }
            >
              Back to Dashboard
            </button>

          </div>

        </main>

      </div>
    );
  }

  // ==========================================================
  // MOCK EXAM
  // ==========================================================

  if (screen === "exam") {
    if (
      !session ||
      !Array.isArray(
        session.questions
      ) ||
      !session.questions[
        currentIndex
      ]
    ) {
      return (
        <div className="app-shell">
          <main className="loading-card">
            Loading exam...
          </main>
        </div>
      );
    }

    const currentQuestion =
      session.questions[
        currentIndex
      ];

    const options =
      Array.isArray(
        currentQuestion.options
      )
        ? [
            ...currentQuestion.options
          ].sort(
            (a, b) =>
              (a.displayOrder ??
                0) -
              (b.displayOrder ??
                0)
          )
        : [];

    const total =
      session.questions.length;

    const partQuestions = getMockPartQuestionCount();
    const partStartIndex = getMockPartStartIndex();
    const partEndIndex = getMockPartEndIndex();
    const partTotal = Math.max(0, partEndIndex - partStartIndex + 1);

    const partAnsweredCount =
      session.questions
        .slice(partStartIndex, partEndIndex + 1)
        .reduce(
          (count, question) =>
            mockAnswers[question.sessionQuestionId] != null
              ? count + 1
              : count,
          0
        );

    const partUnansweredCount =
      Math.max(0, partTotal - partAnsweredCount);

    const progress =
      partTotal > 0
        ? ((currentIndex - partStartIndex + 1) / partTotal) * 100
        : 0;

    const timerMinutes = Math.floor(
      mockRemainingSeconds / 60
    );

    const timerSeconds =
      mockRemainingSeconds % 60;

    const formattedTime = `${String(
      timerMinutes
    ).padStart(2, "0")}:${String(
      timerSeconds
    ).padStart(2, "0")}`;

    const timerUrgent =
      mockRemainingSeconds <= 5 * 60;

    if (showPartTransition) {
      return (
        <div className="exam-shell">
          <header className="exam-header">
            <div>
              <strong>FMGE Mock Test</strong>
              <span>Part 1 completed</span>
            </div>

            <div className="exam-counter">
              <strong>Part 1</strong>
              <span>of 2</span>
            </div>
          </header>

          <main className="question-area">
            <section className="question-card part-transition-card">
              <span className="question-number">Part 1 Complete</span>
              <h1 className="question-text">Ready for Part 2?</h1>

              <p className="notice-box">
                Part 1 has ended. You completed {partAnsweredCount} of {partTotal} questions.
                Part 2 contains the remaining {Math.max(0, total - partTotal)} questions and has a fresh {mockConfig?.partDurationMinutes ?? 150}-minute timer.
                Once you continue, you cannot return to Part 1.
              </p>

              <div className="question-actions" style={{ justifyContent: "flex-end" }}>
                <button
                  type="button"
                  className="primary-button large"
                  onClick={startPartTwo}
                  disabled={loading}
                >
                  Start Part 2 →
                </button>
              </div>
            </section>
          </main>
        </div>
      );
    }

    return (
      <div className="exam-shell">
        <header className="exam-header">
          <div>
            <strong>
              FMGE Mock Test
            </strong>

            <span>
              Session #{session.sessionId} · Part {mockPart} of 2
            </span>
          </div>

          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "16px"
            }}
          >
            <div
              className={
                timerUrgent
                  ? "exam-counter timer urgent"
                  : "exam-counter timer"
              }
              aria-label="Time remaining"
            >
              <span>Time Left</span>
              <strong>{formattedTime}</strong>
            </div>

            <div className="exam-counter">
              <strong>
                {currentIndex - partStartIndex + 1}
              </strong>

              <span>
                / {partTotal}
              </span>
            </div>
          </div>
        </header>

        <div className="progress-bar">
          <div
            style={{
              width: `${progress}%`
            }}
          />
        </div>

        <main className="question-area">
          <div
            style={{
              display: "grid",
              gridTemplateColumns:
                "minmax(0, 1fr) 280px",
              gap: "24px",
              alignItems: "start"
            }}
          >
            <div className="question-card">
              <div className="question-topline">
                <span className="question-number">
                  Question {currentIndex - partStartIndex + 1}
                </span>

                <span className="exam-counter">
                  Part {mockPart} · Answered {partAnsweredCount} / {partTotal}
                </span>
              </div>

              <h1 className="question-text">
                {currentQuestion.questionText}
              </h1>

              {error && (
                <div className="error-box">
                  {error}
                </div>
              )}

              {options.length === 0 ? (
                <div className="error-box">
                  This question has no options.
                </div>
              ) : (
                <div className="options-list">
                  {options.map(
                    (option, index) => {
                      const selected =
                        selectedOptionId ===
                        option.id;

                      const label =
                        option.optionLabel ||
                        String.fromCharCode(
                          65 + index
                        );

                      return (
                        <button
                          key={option.id}
                          type="button"
                          className={
                            selected
                              ? "option-button selected"
                              : "option-button"
                          }
                          onClick={() =>
                            handleOptionSelect(
                              option.id
                            )
                          }
                          disabled={loading}
                        >
                          <span className="option-letter">
                            {label}
                          </span>

                          <span className="option-text">
                            {option.optionText}
                          </span>

                          <span className="option-radio">
                            {selected ? "✓" : ""}
                          </span>
                        </button>
                      );
                    }
                  )}
                </div>
              )}

              <div
                style={{
                  marginTop: "20px",
                  display: "flex",
                  justifyContent: "space-between",
                  gap: "12px",
                  flexWrap: "wrap"
                }}
              >
                <button
                  type="button"
                  className="secondary-button"
                  onClick={handlePrevious}
                  disabled={
                    loading ||
                    currentIndex === partStartIndex
                  }
                >
                  ← Previous
                </button>

                <div
                  style={{
                    display: "flex",
                    gap: "10px",
                    flexWrap: "wrap"
                  }}
                >
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={handleClearAnswer}
                    disabled={
                      loading ||
                      selectedOptionId === null
                    }
                  >
                    Clear
                  </button>

                  <button
                    type="button"
                    className="primary-button"
                    onClick={handleNext}
                    disabled={
                      loading ||
                      currentIndex === total - 1
                    }
                  >
                    {mockPart === 1 && currentIndex === partEndIndex
                      ? "Part 1 Complete →"
                      : "Next →"}
                  </button>
                </div>
              </div>

              <div
                className="notice-box"
                style={{ marginTop: "16px" }}
              >
                You can leave a question unanswered and return to it later.
              </div>
            </div>

            <aside
              className="question-card"
              style={{
                position: "sticky",
                top: "20px"
              }}
            >
              <div className="question-topline">
                <strong>Part {mockPart} Navigator</strong>
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns:
                    "repeat(5, minmax(0, 1fr))",
                  gap: "8px",
                  marginTop: "16px"
                }}
              >
                {session.questions
                  .slice(partStartIndex, partEndIndex + 1)
                  .map((question, partIndex) => {
                    const index = partStartIndex + partIndex;
                    const answered =
                      mockAnswers[
                        question.sessionQuestionId
                      ] != null;

                    const current =
                      index === currentIndex;

                    let className =
                      "secondary-button";

                    if (answered && current) {
                      className =
                        "primary-button";
                    } else if (answered) {
                      className =
                        "primary-button";
                    }

                    return (
                      <button
                        key={
                          question.sessionQuestionId
                        }
                        type="button"
                        className={className}
                        onClick={() =>
                          goToMockQuestion(index)
                        }
                        disabled={loading}
                        style={{
                          minHeight: "42px",
                          padding: "8px",
                          fontSize: "13px"
                        }}
                      >
                        {index + 1}
                      </button>
                    );
                  }
                )}
              </div>

              <div
                style={{
                  marginTop: "18px",
                  display: "grid",
                  gap: "8px"
                }}
              >
                <span>
                  Part answered: <strong>{partAnsweredCount}</strong>
                </span>

                <span>
                  Part unanswered: <strong>{partUnansweredCount}</strong>
                </span>
              </div>

              <button
                type="button"
                className="primary-button large"
                onClick={handleSubmitExam}
                disabled={loading}
                style={{
                  width: "100%",
                  marginTop: "20px"
                }}
              >
                {loading
                  ? "Submitting..."
                  : "Submit Exam"}
              </button>
            </aside>
          </div>
        </main>
      </div>
    );
  }

  // ==========================================================
  // MOCK RESULT
  // ==========================================================

  if (screen === "result") {
    const totalQuestions =
      Number(
        result?.totalQuestions ??
        mockConfig?.totalQuestions ??
        300
      );

    const passingMarks =
      Number(
        mockConfig?.passingMarks ??
        150
      );

    const correctAnswers =
      Number(
        result?.correctAnswers ??
        0
      );

    const incorrectAnswers =
      Number(
        result?.incorrectAnswers ??
        0
      );

    const unansweredQuestions =
      Number(
        result?.unansweredQuestions ??
        Math.max(
          0,
          totalQuestions -
            correctAnswers -
            incorrectAnswers
        )
      );

    // FMGE configuration currently uses:
    // 1 mark per correct answer and no negative marking.
    // The backend remains authoritative; this fallback only keeps
    // the UI correct if an older result response omits score fields.
    const score =
      Number.isFinite(Number(result?.score))
        ? Number(result.score)
        : correctAnswers;

    const percentage =
      Number.isFinite(Number(result?.percentage))
        ? Number(result.percentage)
        : totalQuestions > 0
          ? Math.round(
              (score / totalQuestions) *
              10000
            ) / 100
          : 0;

    const passed =
      typeof result?.passed === "boolean"
        ? result.passed
        : score >= passingMarks;

    return (
      <div className="app-shell">
        <header className="topbar">
          <div className="brand">
            <div className="brand-mark">
              M
            </div>

            <strong>
              MedPrep AI
            </strong>
          </div>

          <button
            className="ghost-button"
            onClick={() => {
              setResult(null);
              setSession(null);
              localStorage.removeItem(MOCK_STORAGE_KEY);
              setCurrentIndex(0);
              setSelectedOptionId(
                null
              );
              setMockAnswers({});
              setSubmittedMockQuestionIds(new Set());
              setError("");
              setScreen("dashboard");
            }}
          >
            Dashboard
          </button>
        </header>

        <main className="result-page">
          <section className="result-hero">
            <p className="eyebrow">
              MOCK TEST RESULT
            </p>

            <h1>
              {passed
                ? "Great job."
                : "Keep preparing."}
            </h1>

            <div className="score-circle">
              <strong>
                {percentage}%
              </strong>

              <span>
                {passed
                  ? "PASSED"
                  : "NOT PASSED"}
              </span>
            </div>
          </section>

          <section className="result-grid">
            <div className="result-card">
              <span>
                Score
              </span>

              <strong>
                {score} / {totalQuestions}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Correct
              </span>

              <strong>
                {correctAnswers}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Incorrect
              </span>

              <strong>
                {incorrectAnswers}
              </strong>
            </div>

            <div className="result-card">
              <span>
                Unanswered
              </span>

              <strong>
                {unansweredQuestions}
              </strong>
            </div>
          </section>

          <button
            className="primary-button large"
            onClick={() => {
              setResult(null);
              setSession(null);
              setCurrentIndex(0);
              setSelectedOptionId(
                null
              );
              setSelectedSubject(null);
              setTopics([]);
              setError("");
              setScreen("dashboard");
            }}
          >
            Back to Dashboard
          </button>
        </main>
      </div>
    );
  }

  return null;
}

export default App;
