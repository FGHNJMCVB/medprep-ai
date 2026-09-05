package com.medprep.config;

import com.medprep.entity.Question;
import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.Subject;
import com.medprep.entity.SubjectSection;
import com.medprep.entity.Topic;

import com.medprep.repository.QuestionOptionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository) {

        return args -> {

            initializeSubjects(subjectRepository);

            initializeTopics(
                    subjectRepository,
                    topicRepository
            );

            initializeQuestions(
                    subjectRepository,
                    topicRepository,
                    questionRepository,
                    questionOptionRepository
            );
        };
    }

    // ==========================================================
    // SUBJECTS
    // ==========================================================

    private void initializeSubjects(
            SubjectRepository subjectRepository) {

        if(subjectRepository.count() > 0) {
            return;
        }

        // ==========================================================
        // PRE & PARA CLINICAL
        // ==========================================================

        subjectRepository.save(
                new Subject(
                        "Anatomy",
                        1,
                        17,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Physiology",
                        2,
                        17,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Biochemistry",
                        3,
                        17,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Pathology",
                        4,
                        13,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Microbiology",
                        5,
                        13,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Pharmacology",
                        6,
                        13,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Forensic Medicine",
                        7,
                        10,
                        SubjectSection.PRE_AND_PARA_CLINICAL
                )
        );

        // ==========================================================
        // CLINICAL
        // ==========================================================

        subjectRepository.save(
                new Subject(
                        "Medicine",
                        8,
                        33,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Psychiatry",
                        9,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Dermatology & STD",
                        10,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Radiotherapy",
                        11,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "General Surgery",
                        12,
                        32,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Anesthesiology",
                        13,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Orthopedics",
                        14,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Radiodiagnosis",
                        15,
                        5,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Pediatrics",
                        16,
                        15,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Ophthalmology",
                        17,
                        15,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Otorhinolaryngology",
                        18,
                        15,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Obstetrics & Gynecology",
                        19,
                        30,
                        SubjectSection.CLINICAL
                )
        );

        subjectRepository.save(
                new Subject(
                        "Community Medicine",
                        20,
                        30,
                        SubjectSection.CLINICAL
                )
        );
    }

    // ==========================================================
    // TOPICS
    // ==========================================================

    private void initializeTopics(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        if(topicRepository.count() > 0) {
            return;
        }

        addTopics(
                subjectRepository,
                topicRepository,
                "Anatomy",
                new String[] {
                        "General Anatomy",
                        "Upper Limb",
                        "Lower Limb",
                        "Thorax",
                        "Abdomen",
                        "Pelvis",
                        "Head and Neck",
                        "Neuroanatomy",
                        "Embryology"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Physiology",
                new String[] {
                        "General Physiology",
                        "Blood",
                        "Nerve and Muscle Physiology",
                        "Cardiovascular System",
                        "Respiratory System",
                        "Gastrointestinal System",
                        "Renal Physiology",
                        "Endocrinology",
                        "Reproductive Physiology"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Biochemistry",
                new String[] {
                        "Biomolecules",
                        "Enzymes",
                        "Carbohydrate Metabolism",
                        "Lipid Metabolism",
                        "Protein Metabolism",
                        "Molecular Biology",
                        "Vitamins and Minerals",
                        "Clinical Biochemistry"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Pathology",
                new String[] {
                        "General Pathology",
                        "Inflammation",
                        "Immunopathology",
                        "Hematology",
                        "Neoplasia",
                        "Cardiovascular Pathology",
                        "Respiratory Pathology",
                        "Gastrointestinal Pathology",
                        "Renal Pathology",
                        "Endocrine Pathology"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Microbiology",
                new String[] {
                        "General Microbiology",
                        "Immunology",
                        "Bacteriology",
                        "Virology",
                        "Mycology",
                        "Parasitology",
                        "Systemic Infections",
                        "Hospital Acquired Infections"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Pharmacology",
                new String[] {
                        "General Pharmacology",
                        "Autonomic Nervous System",
                        "Cardiovascular Drugs",
                        "CNS Drugs",
                        "Antimicrobials",
                        "Endocrine Drugs",
                        "Chemotherapy",
                        "Drugs Affecting Blood"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Forensic Medicine",
                new String[] {
                        "Forensic Basics",
                        "Injuries",
                        "Thanatology",
                        "Forensic Toxicology",
                        "Sexual Offences",
                        "Medicolegal Aspects"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Medicine",
                new String[] {
                        "Cardiology",
                        "Respiratory Medicine",
                        "Gastroenterology",
                        "Nephrology",
                        "Neurology",
                        "Endocrinology",
                        "Rheumatology",
                        "Hematology",
                        "Infectious Diseases",
                        "Emergency Medicine"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Psychiatry",
                new String[] {
                        "General Psychiatry",
                        "Psychotic Disorders",
                        "Mood Disorders",
                        "Anxiety Disorders",
                        "Substance Use Disorders",
                        "Child Psychiatry"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Dermatology & STD",
                new String[] {
                        "Basic Dermatology",
                        "Infective Disorders",
                        "Papulosquamous Disorders",
                        "Autoimmune Skin Disorders",
                        "Skin Tumors",
                        "Sexually Transmitted Diseases"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Radiotherapy",
                new String[] {
                        "Radiation Physics",
                        "Radiobiology",
                        "Radiation Protection",
                        "Radiotherapy Principles",
                        "Cancer Treatment"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "General Surgery",
                new String[] {
                        "Basic Surgery",
                        "Trauma",
                        "Gastrointestinal Surgery",
                        "Hepatobiliary Surgery",
                        "Breast Surgery",
                        "Thyroid Surgery",
                        "Vascular Surgery",
                        "Urology",
                        "Neurosurgery",
                        "Pediatric Surgery"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Anesthesiology",
                new String[] {
                        "General Anesthesia",
                        "Regional Anesthesia",
                        "Airway Management",
                        "Pain Management",
                        "Critical Care",
                        "Anesthetic Drugs"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Orthopedics",
                new String[] {
                        "Trauma",
                        "Fractures and Dislocations",
                        "Bone Tumors",
                        "Arthritis",
                        "Spine",
                        "Pediatric Orthopedics"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Radiodiagnosis",
                new String[] {
                        "Radiology Basics",
                        "Chest Imaging",
                        "Abdominal Imaging",
                        "Neuroradiology",
                        "Musculoskeletal Imaging",
                        "Ultrasound",
                        "CT",
                        "MRI"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Pediatrics",
                new String[] {
                        "Growth and Development",
                        "Neonatology",
                        "Pediatric Infections",
                        "Pediatric Cardiology",
                        "Pediatric Neurology",
                        "Pediatric Emergencies"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Ophthalmology",
                new String[] {
                        "Basic Ophthalmology",
                        "Cornea",
                        "Lens and Cataract",
                        "Glaucoma",
                        "Retina",
                        "Neuro Ophthalmology",
                        "Ocular Trauma"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Otorhinolaryngology",
                new String[] {
                        "Ear",
                        "Nose and Paranasal Sinuses",
                        "Throat",
                        "Head and Neck",
                        "Audiology",
                        "Vertigo"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Obstetrics & Gynecology",
                new String[] {
                        "Obstetrics",
                        "Antenatal Care",
                        "Normal Labour",
                        "High Risk Pregnancy",
                        "Obstetric Emergencies",
                        "Gynecology",
                        "Gynecologic Oncology",
                        "Contraception"
                }
        );

        addTopics(
                subjectRepository,
                topicRepository,
                "Community Medicine",
                new String[] {
                        "Epidemiology",
                        "Biostatistics",
                        "Health Programs",
                        "Preventive Medicine",
                        "Environmental Health",
                        "Nutrition",
                        "Maternal and Child Health",
                        "Occupational Health",
                        "Communicable Diseases",
                        "Non Communicable Diseases"
                }
        );
    }

    // ==========================================================
    // HELPER: ADD TOPICS
    // ==========================================================

    private void addTopics(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            String subjectName,
            String[] topicNames) {

        Subject subject = subjectRepository
                .findByName(subjectName)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Subject not found: " + subjectName
                        )
                );

        for(int i = 0; i < topicNames.length; i++) {

            Topic topic = new Topic(
                    topicNames[i],
                    i + 1,
                    subject
            );

            topicRepository.save(topic);
        }
    }

    // ==========================================================
    // QUESTIONS
    // ==========================================================

    private void initializeQuestions(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository) {

        if(questionRepository.count() > 0) {
            return;
        }

        // ==========================================================
        // PATHOLOGY -> NEOPLASIA
        // ==========================================================

        Subject pathology = subjectRepository
                .findByName("Pathology")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Pathology not found"
                        )
                );

        Topic neoplasia = topicRepository
                .findBySubjectIdOrderByDisplayOrderAsc(pathology.getId())
                .stream()
                .filter(topic ->
                        topic.getName().equals("Neoplasia"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Neoplasia topic not found"
                        )
                );

        Question q1 = new Question(
                "Which tumor suppressor gene is commonly known as the guardian of the genome?",
                "TP53 encodes the p53 protein, which plays a major role in the cellular response to DNA damage and can induce cell-cycle arrest or apoptosis.",
                QuestionDifficulty.EASY,
                true,
                pathology,
                neoplasia
        );

        questionRepository.save(q1);

        questionOptionRepository.save(
                new QuestionOption(
                        "A",
                        "RB1",
                        1,
                        false,
                        q1
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "B",
                        "APC",
                        2,
                        false,
                        q1
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "C",
                        "TP53",
                        3,
                        true,
                        q1
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "D",
                        "BRCA1",
                        4,
                        false,
                        q1
                )
        );

        // ==========================================================
        // ANATOMY -> UPPER LIMB
        // ==========================================================

        Subject anatomy = subjectRepository
                .findByName("Anatomy")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Anatomy not found"
                        )
                );

        Topic upperLimb = topicRepository
                .findBySubjectIdOrderByDisplayOrderAsc(anatomy.getId())
                .stream()
                .filter(topic ->
                        topic.getName().equals("Upper Limb"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Upper Limb topic not found"
                        )
                );

        Question q2 = new Question(
                "Which nerve is most commonly affected in a mid-shaft fracture of the humerus?",
                "The radial nerve runs in the radial groove along the shaft of the humerus and may be injured in mid-shaft humeral fractures.",
                QuestionDifficulty.MEDIUM,
                true,
                anatomy,
                upperLimb
        );

        questionRepository.save(q2);

        questionOptionRepository.save(
                new QuestionOption(
                        "A",
                        "Median nerve",
                        1,
                        false,
                        q2
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "B",
                        "Ulnar nerve",
                        2,
                        false,
                        q2
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "C",
                        "Radial nerve",
                        3,
                        true,
                        q2
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "D",
                        "Musculocutaneous nerve",
                        4,
                        false,
                        q2
                )
        );

        // ==========================================================
        // PHYSIOLOGY -> CARDIOVASCULAR SYSTEM
        // ==========================================================

        Subject physiology = subjectRepository
                .findByName("Physiology")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Physiology not found"
                        )
                );

        Topic cardiovascular = topicRepository
                .findBySubjectIdOrderByDisplayOrderAsc(physiology.getId())
                .stream()
                .filter(topic ->
                        topic.getName().equals(
                                "Cardiovascular System"))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Cardiovascular System topic not found"
                        )
                );

        Question q3 = new Question(
                "Which phase of the cardiac cycle corresponds to ventricular ejection?",
                "During ventricular systole, ventricular pressure rises above arterial pressure and the semilunar valves open, allowing blood to be ejected.",
                QuestionDifficulty.MEDIUM,
                true,
                physiology,
                cardiovascular
        );

        questionRepository.save(q3);

        questionOptionRepository.save(
                new QuestionOption(
                        "A",
                        "Isovolumetric relaxation",
                        1,
                        false,
                        q3
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "B",
                        "Rapid ventricular filling",
                        2,
                        false,
                        q3
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "C",
                        "Ventricular ejection",
                        3,
                        true,
                        q3
                )
        );

        questionOptionRepository.save(
                new QuestionOption(
                        "D",
                        "Atrial systole",
                        4,
                        false,
                        q3
                )
        );
    }
}