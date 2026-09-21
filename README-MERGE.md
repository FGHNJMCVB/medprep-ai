# MedPrep Subject Mock Test Feature

This package contains the complete files needed for:

- Full FMGE mock tests assembled from active database questions.
- Subject-wise mock tests with 50, 100, or 150 questions.
- Topic-balanced and difficulty-balanced subject selection.
- Timers, navigation, resume, submission, results, and history.
- Proportional subject-mock pass marks.

## Merge on macOS

Keep this extracted folder in `~/Downloads`, then run:

```bash
cd ~/Documents/medprep-ai

backup_dir="../medprep-ai-backup-before-subject-mock-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$backup_dir"
cp -R frontend "$backup_dir/"
cp -R backend "$backup_dir/"

ditto ~/Downloads/medprep-subject-mock-feature ./
```

The backup allows you to recover your previous frontend and backend if needed.

## Verify

Backend:

```bash
cd ~/Documents/medprep-ai/backend/medprep-backend
./mvnw test
./mvnw spring-boot:run
```

Frontend, in a second terminal:

```bash
cd ~/Documents/medprep-ai/frontend
npm install
npm run dev
```

No manual database migration is required because the session type is stored as
a string and Hibernate is configured with `ddl-auto=update`.

## Files included

- `frontend/src/App.jsx`
- `frontend/src/api.js`
- `frontend/src/index.css`
- `backend/medprep-backend/src/main/java/com/medprep/controller/MockTestController.java`
- `backend/medprep-backend/src/main/java/com/medprep/dto/MockTestHistoryResponse.java`
- `backend/medprep-backend/src/main/java/com/medprep/dto/SubjectMockTestStartRequest.java`
- `backend/medprep-backend/src/main/java/com/medprep/entity/PracticeSessionType.java`
- `backend/medprep-backend/src/main/java/com/medprep/service/MockTestGenerationService.java`
- `backend/medprep-backend/src/main/java/com/medprep/service/MockTestResultService.java`
- `backend/medprep-backend/src/main/java/com/medprep/service/MockTestService.java`
- `backend/medprep-backend/src/main/java/com/medprep/service/MockTestSubmissionService.java`
