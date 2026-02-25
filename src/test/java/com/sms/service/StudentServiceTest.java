package com.sms.service;

import com.sms.domain.Student;
import com.sms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StudentService using a simple in-memory fake repository.
 * No database connection is needed for these tests.
 */
class StudentServiceTest {

    private StudentService service;
    private List<Student> fakeDb;

    @BeforeEach
    void setUp() {
        fakeDb = new ArrayList<>();
        fakeDb.add(make("S001", "Alice", "CS",  100, 3.8));
        fakeDb.add(make("S002", "Bob",   "CS",  200, 1.5));
        fakeDb.add(make("S003", "Carol", "Math",300, 2.5));
        fakeDb.add(make("S004", "Dave",  "Math",100, 0.9));
        fakeDb.add(make("S005", "Eve",   "CS",  400, 3.9));

        StudentRepository fakeRepo = buildFakeRepo();
        service = new StudentService(fakeRepo, new ValidationService());
    }

    @Test
    void test_topPerformers_returnsTopByGpa() {
        List<Student> top = service.getTopPerformers(3, null, null);
        assertEquals(3, top.size());
        assertEquals("S005", top.get(0).getStudentId()); // GPA 3.9
        assertEquals("S001", top.get(1).getStudentId()); // GPA 3.8
    }

    @Test
    void test_topPerformers_filteredByProgramme() {
        List<Student> top = service.getTopPerformers(10, "Math", null);
        assertTrue(top.stream().allMatch(s -> "Math".equals(s.getProgramme())));
    }

    @Test
    void test_atRisk_defaultThreshold() {
        List<Student> risk = service.getAtRiskStudents(2.0);
        // Bob (1.5) and Dave (0.9) are below 2.0
        assertEquals(2, risk.size());
        assertTrue(risk.stream().allMatch(s -> s.getGpa() < 2.0));
    }

    @Test
    void test_atRisk_customThreshold() {
        List<Student> risk = service.getAtRiskStudents(3.0);
        // Bob, Carol, Dave all below 3.0
        assertEquals(3, risk.size());
    }

    @Test
    void test_gpaDistribution_correctBands() {
        Map<String, Long> dist = service.getGpaDistribution();
        assertEquals(1L, dist.get("0.0 – 1.0")); // Dave: 0.9
        assertEquals(1L, dist.get("1.0 – 2.0")); // Bob: 1.5
        assertEquals(1L, dist.get("2.0 – 3.0")); // Carol: 2.5
        assertEquals(2L, dist.get("3.0 – 4.0")); // Alice: 3.8, Eve: 3.9
    }

    @Test
    void test_averageGpa_calculatedCorrectly() {
        double avg = service.getAverageGpa();
        double expected = (3.8 + 1.5 + 2.5 + 0.9 + 3.9) / 5.0;
        assertEquals(expected, avg, 0.001);
    }

    @Test
    void test_programmeSummary_groupedCorrectly() {
        List<Map<String, Object>> summary = service.getProgrammeSummary();
        assertEquals(2, summary.size());
        Optional<Map<String, Object>> cs = summary.stream()
                .filter(m -> "CS".equals(m.get("Programme"))).findFirst();
        assertTrue(cs.isPresent());
        assertEquals(3, cs.get().get("Total"));
    }

    @Test
    void test_addStudent_duplicateId_throwsException() {
        Student dup = make("S001", "New Person", "CS", 100, 3.0);
        assertThrows(IllegalArgumentException.class, () -> service.addStudent(dup));
    }

    // ── Fake in-memory repository ─────────────────────────────────────────────

    private Student make(String id, String name, String prog, int level, double gpa) {
        return new Student(id, name, prog, level, gpa,
                name.toLowerCase() + "@test.com", "0244000001",
                LocalDate.now(), "Active");
    }

    private StudentRepository buildFakeRepo() {
        return new StudentRepository() {
            @Override public void save(Student s)          { fakeDb.add(s); }
            @Override public void update(Student s)        { fakeDb.replaceAll(e -> e.getStudentId().equals(s.getStudentId()) ? s : e); }
            @Override public void delete(String id)        { fakeDb.removeIf(s -> s.getStudentId().equals(id)); }
            @Override public Optional<Student> findById(String id) { return fakeDb.stream().filter(s -> s.getStudentId().equals(id)).findFirst(); }
            @Override public List<Student> findAll()       { return new ArrayList<>(fakeDb); }
            @Override public List<Student> search(String q){ return fakeDb.stream().filter(s -> s.getStudentId().contains(q) || s.getFullName().toLowerCase().contains(q.toLowerCase())).toList(); }
            @Override public List<Student> filter(String prog, Integer level, String status) {
                return fakeDb.stream()
                    .filter(s -> prog   == null || prog.isEmpty()   || prog.equals(s.getProgramme()))
                    .filter(s -> level  == null                       || level == s.getLevel())
                    .filter(s -> status == null || status.isEmpty() || status.equals(s.getStatus()))
                    .toList();
            }
            @Override public List<String> findAllProgrammes() { return fakeDb.stream().map(Student::getProgramme).distinct().sorted().toList(); }
            @Override public boolean existsById(String id) { return fakeDb.stream().anyMatch(s -> s.getStudentId().equals(id)); }
        };
    }
}
