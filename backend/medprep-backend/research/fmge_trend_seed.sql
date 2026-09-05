BEGIN;

-- ============================================================
-- FMGE RESEARCH-INFORMED TREND PRIORITY SEED
--
-- IMPORTANT:
-- These values are PRIORITY/RECENCY PROXIES for development.
-- They are NOT claimed to be official NBEMS question counts.
--
-- Source basis:
-- Recent FMGE recall/exam-analysis material emphasizing:
--   * recurring concepts
--   * clinical/application questions
--   * high-yield systems
--   * image-based questions
--   * repeated core concepts
--
-- Once we build the full recall corpus, these proxy values
-- should be replaced by measured frequencies.
-- ============================================================


-- ============================================================
-- ANATOMY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(1, 8, 'Cranial nerves and lesions',
 5, 3, 4, 0.95, 0.60, 0.90, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(1, 2, 'Brachial plexus and peripheral nerve injuries',
 5, 3, 4, 0.95, 0.45, 0.90, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(1, 7, 'Head and neck anatomy and foramina',
 4, 3, 4, 0.90, 0.55, 0.85, 0.52, true,
 'Research-informed FMGE recurring-concept seed'),

(1, 5, 'Abdominal anatomy and peritoneal relations',
 3, 2, 3, 0.80, 0.35, 0.75, 0.43, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- PHYSIOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(2, 13, 'Cardiac cycle and hemodynamics',
 5, 3, 4, 0.95, 0.35, 0.90, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(2, 14, 'Respiratory physiology and V/Q relationships',
 5, 3, 4, 0.95, 0.30, 0.90, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(2, 16, 'Renal physiology and acid-base balance',
 5, 3, 4, 0.95, 0.25, 0.95, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(2, 17, 'Endocrine physiology and feedback mechanisms',
 4, 2, 3, 0.90, 0.20, 0.85, 0.47, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- BIOCHEMISTRY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(3, 21, 'Carbohydrate metabolism and metabolic disorders',
 4, 3, 4, 0.90, 0.15, 0.85, 0.49, false,
 'Research-informed FMGE recurring-concept seed'),

(3, 25, 'Vitamins and mineral deficiencies',
 5, 3, 4, 0.90, 0.15, 0.85, 0.54, true,
 'Research-informed FMGE recurring-concept seed'),

(3, 20, 'Enzyme kinetics and inhibition',
 4, 2, 3, 0.80, 0.10, 0.75, 0.43, false,
 'Research-informed FMGE recurring-concept seed'),

(3, 24, 'Molecular biology and DNA replication',
 4, 2, 3, 0.80, 0.10, 0.80, 0.43, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- PATHOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(4, 30, 'Hematology and anemias',
 5, 3, 4, 0.95, 0.55, 0.90, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(4, 31, 'Neoplasia, tumor markers and cancer pathology',
 5, 3, 4, 0.95, 0.75, 0.95, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(4, 29, 'Immunopathology and hypersensitivity',
 4, 2, 3, 0.90, 0.45, 0.90, 0.49, false,
 'Research-informed FMGE recurring-concept seed'),

(4, 32, 'Cardiovascular pathology',
 4, 2, 3, 0.90, 0.60, 0.90, 0.49, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- MICROBIOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(5, 39, 'Bacteriology and laboratory diagnosis',
 5, 3, 4, 0.90, 0.45, 0.90, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(5, 40, 'Virology: HIV and clinically important viruses',
 5, 3, 4, 0.95, 0.40, 0.95, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(5, 43, 'Systemic infections and tuberculosis',
 5, 3, 4, 0.95, 0.50, 0.95, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(5, 42, 'Parasitology and malaria',
 4, 3, 4, 0.95, 0.35, 0.90, 0.52, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- PHARMACOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(6, 49, 'Antimicrobials: mechanism and drug of choice',
 5, 3, 4, 0.95, 0.20, 0.95, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(6, 47, 'Cardiovascular drugs and clinical use',
 5, 3, 4, 0.95, 0.15, 0.95, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(6, 48, 'CNS drugs and adverse effects',
 4, 3, 4, 0.90, 0.15, 0.90, 0.51, true,
 'Research-informed FMGE recurring-concept seed'),

(6, 50, 'Endocrine drugs and treatment choices',
 4, 2, 3, 0.90, 0.15, 0.85, 0.46, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- FORENSIC MEDICINE
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(7, 54, 'Injuries and medico-legal interpretation',
 4, 2, 3, 0.90, 0.45, 0.80, 0.45, false,
 'Research-informed FMGE recurring-concept seed'),

(7, 55, 'Thanatology and postmortem changes',
 4, 2, 3, 0.80, 0.35, 0.75, 0.42, false,
 'Research-informed FMGE recurring-concept seed'),

(7, 56, 'Forensic toxicology and poisoning',
 5, 3, 4, 0.90, 0.40, 0.85, 0.51, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- MEDICINE
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(8, 59, 'Cardiology: MI, heart failure and hypertension',
 5, 3, 4, 0.98, 0.70, 0.98, 0.59, true,
 'Research-informed FMGE recurring-concept seed'),

(8, 60, 'Respiratory medicine: common clinical syndromes',
 5, 3, 4, 0.98, 0.75, 0.95, 0.59, true,
 'Research-informed FMGE recurring-concept seed'),

(8, 63, 'Neurology: stroke, epilepsy and CNS infections',
 5, 3, 4, 0.98, 0.65, 0.98, 0.59, true,
 'Research-informed FMGE recurring-concept seed'),

(8, 67, 'Infectious diseases: TB, HIV and malaria',
 5, 3, 4, 0.98, 0.50, 0.98, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(8, 62, 'Nephrology: AKI, CKD and electrolyte disorders',
 4, 2, 3, 0.95, 0.35, 0.95, 0.49, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- PSYCHIATRY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(9, 70, 'Psychotic disorders and antipsychotics',
 4, 2, 3, 0.90, 0.10, 0.85, 0.45, false,
 'Research-informed FMGE recurring-concept seed'),

(9, 71, 'Mood disorders and treatment',
 4, 2, 3, 0.90, 0.10, 0.85, 0.45, false,
 'Research-informed FMGE recurring-concept seed'),

(9, 73, 'Substance use disorders',
 4, 2, 3, 0.90, 0.10, 0.85, 0.45, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- DERMATOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(10, 76, 'Infective dermatology',
 4, 2, 3, 0.90, 0.85, 0.80, 0.47, false,
 'Research-informed FMGE recurring-concept seed'),

(10, 77, 'Papulosquamous disorders',
 5, 3, 4, 0.90, 0.95, 0.85, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(10, 80, 'Sexually transmitted diseases',
 5, 3, 4, 0.95, 0.85, 0.90, 0.55, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- RADIOTHERAPY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(11, 82, 'Radiobiology principles',
 4, 2, 3, 0.80, 0.60, 0.75, 0.43, false,
 'Research-informed FMGE recurring-concept seed'),

(11, 84, 'Radiotherapy principles and fractionation',
 4, 2, 3, 0.85, 0.55, 0.80, 0.44, false,
 'Research-informed FMGE recurring-concept seed'),

(11, 85, 'Cancer treatment concepts',
 4, 2, 3, 0.90, 0.60, 0.90, 0.47, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- GENERAL SURGERY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(12, 87, 'Trauma and emergency surgical management',
 5, 3, 4, 0.98, 0.65, 0.95, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(12, 88, 'Gastrointestinal surgery and acute abdomen',
 5, 3, 4, 0.98, 0.55, 0.95, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(12, 91, 'Thyroid surgery and neck swellings',
 4, 2, 3, 0.90, 0.60, 0.90, 0.49, false,
 'Research-informed FMGE recurring-concept seed'),

(12, 90, 'Breast pathology and breast surgery',
 4, 2, 3, 0.95, 0.75, 0.90, 0.51, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- ANESTHESIOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(13, 98, 'Airway management',
 5, 3, 4, 0.98, 0.40, 0.90, 0.55, true,
 'Research-informed FMGE recurring-concept seed'),

(13, 100, 'Critical care and resuscitation',
 4, 3, 4, 0.98, 0.35, 0.95, 0.53, true,
 'Research-informed FMGE recurring-concept seed'),

(13, 101, 'Anesthetic drugs and adverse effects',
 4, 2, 3, 0.90, 0.20, 0.90, 0.47, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- ORTHOPEDICS
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(14, 103, 'Fractures and dislocations',
 5, 3, 4, 0.98, 0.85, 0.95, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(14, 102, 'Trauma and emergency orthopedics',
 5, 3, 4, 0.98, 0.75, 0.95, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(14, 106, 'Spine disorders and trauma',
 4, 2, 3, 0.95, 0.80, 0.90, 0.50, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- RADIODIAGNOSIS
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(15, 109, 'Chest imaging',
 5, 3, 4, 0.95, 0.98, 0.90, 0.59, true,
 'Research-informed FMGE recurring-concept seed'),

(15, 111, 'Neuroradiology',
 5, 3, 4, 0.95, 0.98, 0.95, 0.59, true,
 'Research-informed FMGE recurring-concept seed'),

(15, 112, 'Musculoskeletal imaging',
 4, 2, 3, 0.90, 0.98, 0.85, 0.52, true,
 'Research-informed FMGE recurring-concept seed'),

(15, 110, 'Abdominal imaging',
 4, 2, 3, 0.90, 0.95, 0.85, 0.50, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- PEDIATRICS
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(16, 117, 'Neonatology',
 5, 3, 4, 0.98, 0.50, 0.95, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(16, 118, 'Pediatric infections',
 5, 3, 4, 0.98, 0.45, 0.95, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(16, 119, 'Pediatric cardiology',
 4, 2, 3, 0.95, 0.45, 0.90, 0.50, true,
 'Research-informed FMGE recurring-concept seed'),

(16, 121, 'Pediatric emergencies',
 4, 3, 4, 0.98, 0.40, 0.95, 0.53, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- OPHTHALMOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(17, 125, 'Glaucoma',
 5, 3, 4, 0.95, 0.95, 0.90, 0.57, true,
 'Research-informed FMGE recurring-concept seed'),

(17, 126, 'Retina and retinal vascular disease',
 5, 3, 4, 0.95, 0.98, 0.95, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(17, 127, 'Neuro-ophthalmology',
 4, 2, 3, 0.95, 0.80, 0.95, 0.51, true,
 'Research-informed FMGE recurring-concept seed'),

(17, 124, 'Lens and cataract',
 4, 2, 3, 0.90, 0.85, 0.85, 0.49, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- ENT
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(18, 129, 'Ear disease and hearing loss',
 5, 3, 4, 0.90, 0.60, 0.85, 0.53, true,
 'Research-informed FMGE recurring-concept seed'),

(18, 130, 'Nose and paranasal sinus disorders',
 4, 2, 3, 0.90, 0.70, 0.80, 0.48, false,
 'Research-informed FMGE recurring-concept seed'),

(18, 134, 'Vertigo and vestibular disorders',
 4, 2, 3, 0.95, 0.65, 0.90, 0.49, false,
 'Research-informed FMGE recurring-concept seed'),

(18, 132, 'Head and neck lesions',
 4, 2, 3, 0.95, 0.70, 0.90, 0.50, true,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- OBSTETRICS & GYNECOLOGY
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(19, 139, 'Obstetric emergencies',
 5, 3, 4, 0.98, 0.60, 0.98, 0.58, true,
 'Research-informed FMGE recurring-concept seed'),

(19, 138, 'High-risk pregnancy',
 5, 3, 4, 0.98, 0.45, 0.95, 0.56, true,
 'Research-informed FMGE recurring-concept seed'),

(19, 135, 'Core obstetrics and antenatal care',
 5, 3, 4, 0.95, 0.35, 0.90, 0.54, true,
 'Research-informed FMGE recurring-concept seed'),

(19, 140, 'Gynecology and common gynecologic disorders',
 4, 2, 3, 0.95, 0.50, 0.90, 0.49, false,
 'Research-informed FMGE recurring-concept seed')


ON CONFLICT (subject_id, topic_id, concept_tag)
DO UPDATE SET
    historical_frequency = EXCLUDED.historical_frequency,
    recent_frequency = EXCLUDED.recent_frequency,
    recurrence_years = EXCLUDED.recurrence_years,
    clinical_weight = EXCLUDED.clinical_weight,
    image_weight = EXCLUDED.image_weight,
    integrated_weight = EXCLUDED.integrated_weight,
    trend_score = EXCLUDED.trend_score,
    high_yield = EXCLUDED.high_yield,
    source_reference = EXCLUDED.source_reference;


-- ============================================================
-- COMMUNITY MEDICINE / PSM
-- ============================================================

INSERT INTO question_trends
(
    subject_id,
    topic_id,
    concept_tag,
    historical_frequency,
    recent_frequency,
    recurrence_years,
    clinical_weight,
    image_weight,
    integrated_weight,
    trend_score,
    high_yield,
    source_reference
)
VALUES
(20, 143, 'Epidemiology and study designs',
 5, 3, 4, 0.90, 0.10, 0.85, 0.51, true,
 'Research-informed FMGE recurring-concept seed'),

(20, 145, 'National health programs',
 5, 3, 4, 0.90, 0.10, 0.90, 0.52, true,
 'Research-informed FMGE recurring-concept seed'),

(20, 144, 'Biostatistics',
 5, 3, 4, 0.85, 0.10, 0.80, 0.48, false,
 'Research-informed FMGE recurring-concept seed'),

(20, 151, 'Communicable disease control',
 5, 3, 4, 0.90, 0.10, 0.90, 0.52, true,
 'Research-informed FMGE recurring-concept seed'),

(20, 152, 'Non-communicable disease prevention',
 4, 2, 3, 0.85, 0.10, 0.85, 0.46, false,
 'Research-informed FMGE recurring-concept seed');


COMMIT;