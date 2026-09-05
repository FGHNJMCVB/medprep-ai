const API_BASE_URL = "http://localhost:8080";

// ==========================================================
// COMMON API REQUEST
// ==========================================================

export async function apiRequest(
  path,
  options = {}
) {
  const token =
    localStorage.getItem("studentToken");

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization =
      `Bearer ${token}`;
  }

  let response;

  try {
    response = await fetch(
      `${API_BASE_URL}${path}`,
      {
        ...options,
        headers
      }
    );
  } catch (error) {
    console.error(
      "API connection error:",
      error
    );

    throw new Error(
      "Unable to connect to the MedPrep server."
    );
  }

  if (response.status === 204) {
    return null;
  }

  const text =
    await response.text();

  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      typeof data === "object" &&
      data !== null &&
      data.message
        ? data.message
        : `Request failed with status ${response.status}`;

    throw new Error(message);
  }

  return data;
}


// ==========================================================
// AUTH
// ==========================================================

export function login(
  email,
  password
) {
  return apiRequest(
    "/api/auth/login",
    {
      method: "POST",
      body: JSON.stringify({
        email,
        password
      })
    }
  );
}


export function register(
  firstName,
  lastName,
  email,
  password
) {
  return apiRequest(
    "/api/auth/register",
    {
      method: "POST",
      body: JSON.stringify({
        firstName,
        lastName,
        email,
        password
      })
    }
  );
}


// ==========================================================
// SUBJECTS
// ==========================================================

export function getSubjects() {
  return apiRequest(
    "/api/subjects"
  );
}


// ==========================================================
// TOPICS
// ==========================================================

export function getTopicsBySubject(
  subjectId
) {
  return apiRequest(
    `/api/subjects/${subjectId}/topics`
  );
}


// ==========================================================
// MOCK TEST
// ==========================================================

export function getMockConfig() {
  return apiRequest(
    "/api/mock-tests/config"
  );
}


export function getCurrentMockTest() {
  return apiRequest(
    "/api/mock-tests/current"
  );
}


export function startMockTest(
  configId
) {
  return apiRequest(
    "/api/mock-tests/start",
    {
      method: "POST",
      body: JSON.stringify({
        configId
      })
    }
  );
}


export function submitAnswer(
  sessionId,
  sessionQuestionId,
  selectedOptionId,
  timeTakenSeconds
) {
  return apiRequest(
    `/api/mock-tests/${sessionId}/questions/${sessionQuestionId}/answer`,
    {
      method: "POST",
      body: JSON.stringify({
        selectedOptionId,
        timeTakenSeconds
      })
    }
  );
}


export function submitMockTest(
  sessionId
) {
  return apiRequest(
    `/api/mock-tests/${sessionId}/submit`,
    {
      method: "POST"
    }
  );
}


export function getMockResult(
  sessionId
) {
  return apiRequest(
    `/api/mock-tests/${sessionId}/result`
  );
}


export function getMockHistory() {
  return apiRequest(
    "/api/mock-tests/history"
  );
}


// ==========================================================
// PRACTICE
// ==========================================================

export function startPractice({
  subjectId = null,
  topicId = null,
  numberOfQuestions = 5
}) {
  const body = {
    numberOfQuestions
  };

  if (
    topicId !== null &&
    topicId !== undefined
  ) {
    body.topicId = topicId;
  } else if (
    subjectId !== null &&
    subjectId !== undefined
  ) {
    body.subjectId = subjectId;
  }

  return apiRequest(
    "/api/practice/start",
    {
      method: "POST",
      body: JSON.stringify(body)
    }
  );
}


export function getPracticeSession(
  sessionId
) {
  return apiRequest(
    `/api/practice/${sessionId}`
  );
}


export function submitPracticeAnswer(
  sessionId,
  sessionQuestionId,
  selectedOptionId,
  timeTakenSeconds
) {
  return apiRequest(
    `/api/practice/${sessionId}/questions/${sessionQuestionId}/answer`,
    {
      method: "POST",
      body: JSON.stringify({
        selectedOptionId,
        timeTakenSeconds
      })
    }
  );
}


export function finishPractice(
  sessionId
) {
  return apiRequest(
    `/api/practice/${sessionId}/finish`,
    {
      method: "POST"
    }
  );
}


export function getPracticeResult(
  sessionId
) {
  return apiRequest(
    `/api/practice/${sessionId}/result`
  );
}

// ==========================================================
// GET PRACTICE REVIEW
// ==========================================================

export function getPracticeReview(
  sessionId
) {
  return apiRequest(
    `/api/practice/${sessionId}/review`
  );
}

export function getPracticeHistory() {
  return apiRequest("/api/practice/history");
}
