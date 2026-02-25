package com.sms.repository;

import com.sms.domain.Student;
import com.sms.util.AppLogger;
import com.sms.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of StudentRepository.
 * Every query uses prepared statements – no string concatenation with user input.
 */
public class SQLiteStudentRepository implements StudentRepository {

    // ── Save ──────────────────────────────────────────────────────────────────

    @Override
    public void save(Student s) {
        String sql = """
                INSERT INTO students
                    (student_id, full_name, programme, level, gpa, email, phone_number, date_added, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            setStudentParams(ps, s);
            ps.executeUpdate();
            AppLogger.info("Student added: ID=" + s.getStudentId());
        } catch (SQLException e) {
            AppLogger.error("DB error on save: " + e.getMessage());
            throw new RuntimeException("Could not save student: " + e.getMessage(), e);
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update(Student s) {
        String sql = """
                UPDATE students
                SET full_name=?, programme=?, level=?, gpa=?, email=?, phone_number=?, date_added=?, status=?
                WHERE student_id=?
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getProgramme());
            ps.setInt(3, s.getLevel());
            ps.setDouble(4, s.getGpa());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getPhoneNumber());
            ps.setString(7, s.getDateAdded().toString());
            ps.setString(8, s.getStatus());
            ps.setString(9, s.getStudentId());
            ps.executeUpdate();
            AppLogger.info("Student updated: ID=" + s.getStudentId());
        } catch (SQLException e) {
            AppLogger.error("DB error on update: " + e.getMessage());
            throw new RuntimeException("Could not update student: " + e.getMessage(), e);
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.executeUpdate();
            AppLogger.info("Student deleted: ID=" + studentId);
        } catch (SQLException e) {
            AppLogger.error("DB error on delete: " + e.getMessage());
            throw new RuntimeException("Could not delete student: " + e.getMessage(), e);
        }
    }

    // ── Find by ID ────────────────────────────────────────────────────────────

    @Override
    public Optional<Student> findById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("DB error on findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ── Find All ──────────────────────────────────────────────────────────────

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY full_name";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("DB error on findAll: " + e.getMessage());
        }
        return list;
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Override
    public List<Student> search(String query) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE LOWER(student_id) LIKE ? OR LOWER(full_name) LIKE ?";
        String pattern = "%" + query.toLowerCase() + "%";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("DB error on search: " + e.getMessage());
        }
        return list;
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    @Override
    public List<Student> filter(String programme, Integer level, String status) {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1");
        if (programme != null && !programme.isEmpty()) sql.append(" AND programme = ?");
        if (level != null)                             sql.append(" AND level = ?");
        if (status != null && !status.isEmpty())       sql.append(" AND status = ?");
        sql.append(" ORDER BY full_name");

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            int idx = 1;
            if (programme != null && !programme.isEmpty()) ps.setString(idx++, programme);
            if (level != null)                             ps.setInt(idx++, level);
            if (status != null && !status.isEmpty())       ps.setString(idx, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            AppLogger.error("DB error on filter: " + e.getMessage());
        }
        return list;
    }

    // ── Find All Programmes ───────────────────────────────────────────────────

    @Override
    public List<String> findAllProgrammes() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT programme FROM students ORDER BY programme";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("programme"));
        } catch (SQLException e) {
            AppLogger.error("DB error on findAllProgrammes: " + e.getMessage());
        }
        return list;
    }

    // ── Exists ────────────────────────────────────────────────────────────────

    @Override
    public boolean existsById(String studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            AppLogger.error("DB error on existsById: " + e.getMessage());
        }
        return false;
    }

    // ── Helper: map a ResultSet row to a Student ──────────────────────────────

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("full_name"),
                rs.getString("programme"),
                rs.getInt("level"),
                rs.getDouble("gpa"),
                rs.getString("email"),
                rs.getString("phone_number"),
                LocalDate.parse(rs.getString("date_added")),
                rs.getString("status")
        );
    }

    // ── Helper: set INSERT parameters ─────────────────────────────────────────

    private void setStudentParams(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getStudentId());
        ps.setString(2, s.getFullName());
        ps.setString(3, s.getProgramme());
        ps.setInt(4, s.getLevel());
        ps.setDouble(5, s.getGpa());
        ps.setString(6, s.getEmail());
        ps.setString(7, s.getPhoneNumber());
        ps.setString(8, s.getDateAdded().toString());
        ps.setString(9, s.getStatus());
    }
}
