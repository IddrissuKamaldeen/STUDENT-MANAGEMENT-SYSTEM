package com.sms.ui.controllers;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sms.domain.Student;
import com.sms.service.StudentService;
import com.sms.util.ServiceLocator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controls the Students screen. Handles displaying, adding, editing,
 * deleting, searching, and filtering students.
 */
public class StudentsController {

    private static final Logger LOGGER = Logger.getLogger(StudentsController.class.getName());

    // ── Table ─────────────────────────────────────────────────────────────────
    @FXML private TableView<Student> tblStudents;
    @FXML private TableColumn<Student, String> colId, colName, colProg, colEmail, colPhone, colDate, colStatus;
    @FXML private TableColumn<Student, String> colLevel, colGpa;  // kept as String columns for display

    // ── Filters ───────────────────────────────────────────────────────────────
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbProgramme, cbStatus;
    @FXML private ComboBox<Integer> cbLevel;

    // ── Form ──────────────────────────────────────────────────────────────────
    @FXML private VBox formPanel;
    @FXML private Label lblFormTitle, lblFormError;
    @FXML private TextField fldId, fldName, fldProgramme, fldGpa, fldEmail, fldPhone;
    @FXML private ComboBox<Integer> fldLevel;
    @FXML private ComboBox<String> fldStatus;

    // ── Status bar ────────────────────────────────────────────────────────────
    @FXML private Label lblStatus;

    private final StudentService studentService = ServiceLocator.getStudentService();
    private boolean editMode = false;

    @FXML
    public void initialize() {
        setupColumns();
        setupFilterOptions();
        refreshTable();
    }

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colProg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProgramme()));
        colLevel.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getLevel())));
        colGpa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getGpa())));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhoneNumber()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateAdded().toString()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Fix numeric sorting for GPA (string column → custom comparator)
        colGpa.setComparator(Comparator.comparingDouble(s -> {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0.0; // fallback for invalid data
            }
        }));

        // Optional: level can also use numeric sort if desired
        colLevel.setComparator(Comparator.comparingInt(Integer::parseInt));
    }

    private void setupFilterOptions() {
        cbLevel.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700));
        cbStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));

        fldLevel.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700));
        fldStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));
        fldStatus.setValue("Active");

        refreshProgrammeFilter();
    }

    private void refreshProgrammeFilter() {
        try {
            List<String> programmes = studentService.getAllProgrammes();
            cbProgramme.setItems(FXCollections.observableArrayList(programmes));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load programmes for filter", e);
            cbProgramme.setItems(FXCollections.emptyObservableList());
        }
    }

    // ── Table & Data Refresh ──────────────────────────────────────────────────

    @FXML
    public void refreshTable() {
        try {
            List<Student> students = studentService.getAllStudents();
            tblStudents.setItems(FXCollections.observableArrayList(students));
            lblStatus.setText("Showing " + students.size() + " student(s).");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to refresh student table", e);
            lblStatus.setText("Error loading students.");
        }
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    @FXML
    public void onSearch() {
        String query = txtSearch.getText().trim();
        List<Student> results = studentService.searchStudents(query);
        tblStudents.setItems(FXCollections.observableArrayList(results));
        lblStatus.setText(results.size() + " result(s) for '" + query + "'.");
    }

    @FXML
    public void onFilter() {
        String programme = cbProgramme.getValue();
        Integer level    = cbLevel.getValue();
        String status    = cbStatus.getValue();

        List<Student> results = studentService.filterStudents(programme, level, status);
        tblStudents.setItems(FXCollections.observableArrayList(results));
        lblStatus.setText("Filter applied: " + results.size() + " student(s).");
    }

    @FXML
    public void clearFilters() {
        txtSearch.clear();
        cbProgramme.setValue(null);
        cbLevel.setValue(null);
        cbStatus.setValue(null);
        refreshTable();
    }

    // ── Form: Add / Edit ──────────────────────────────────────────────────────

    @FXML
    public void showAddForm() {
        editMode = false;
        lblFormTitle.setText("Add Student");
        clearForm();
        fldId.setDisable(false);
        showForm();
    }

    @FXML
    public void showEditForm() {
        Student selected = tblStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit.");
            return;
        }
        editMode = true;
        lblFormTitle.setText("Edit Student");
        populateForm(selected);
        fldId.setDisable(true); // ID cannot change
        showForm();
    }

    private void populateForm(Student s) {
        fldId.setText(s.getStudentId());
        fldName.setText(s.getFullName());
        fldProgramme.setText(s.getProgramme());
        fldLevel.setValue(s.getLevel());
        fldGpa.setText(String.format("%.2f", s.getGpa()));
        fldEmail.setText(s.getEmail());
        fldPhone.setText(s.getPhoneNumber());
        fldStatus.setValue(s.getStatus());
    }

    // ── Form: Save ────────────────────────────────────────────────────────────

    @FXML
    public void saveStudent() {
        lblFormError.setText("");

        if (!validateForm()) {
            return;
        }

        try {
            Student s = buildStudentFromForm();

            if (editMode) {
                studentService.updateStudent(s);
                lblStatus.setText("Student updated: " + s.getStudentId());
            } else {
                studentService.addStudent(s);
                lblStatus.setText("Student added: " + s.getStudentId());
            }

            hideForm();
            refreshTable();
            refreshProgrammeFilter();

        } catch (IllegalArgumentException e) {
            lblFormError.setText(e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving student", e);
            lblFormError.setText("Failed to save student. Please check input.");
        }
    }

    private boolean validateForm() {
        if (fldId.getText().trim().isEmpty()) {
            lblFormError.setText("Student ID is required.");
            return false;
        }
        if (fldName.getText().trim().isEmpty()) {
            lblFormError.setText("Full name is required.");
            return false;
        }
        if (fldProgramme.getText().trim().isEmpty()) {
            lblFormError.setText("Programme is required.");
            return false;
        }
        if (fldLevel.getValue() == null) {
            lblFormError.setText("Level is required.");
            return false;
        }
        return true;
    }

    private Student buildStudentFromForm() {
        Student s = new Student();
        s.setStudentId(fldId.getText().trim());
        s.setFullName(fldName.getText().trim());
        s.setProgramme(fldProgramme.getText().trim());
        s.setLevel(fldLevel.getValue());

        String gpaStr = fldGpa.getText().trim();
        try {
            s.setGpa(gpaStr.isEmpty() ? 0.0 : Double.parseDouble(gpaStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GPA must be a valid number (e.g. 3.45)");
        }

        s.setEmail(fldEmail.getText().trim());
        s.setPhoneNumber(fldPhone.getText().trim());
        s.setDateAdded(LocalDate.now());
        s.setStatus(fldStatus.getValue() != null ? fldStatus.getValue() : "Active");

        return s;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @FXML
    public void deleteStudent() {
        Student selected = tblStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete student '" + selected.getFullName() + "'? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                studentService.deleteStudent(selected.getStudentId());
                refreshTable();
                refreshProgrammeFilter();
                lblStatus.setText("Deleted: " + selected.getStudentId());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting student", e);
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Could not delete student.");
            }
        }
    }

    // ── Form visibility & helpers ─────────────────────────────────────────────

    private void showForm() {
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    @FXML
    public void hideForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        clearForm();
    }

    private void clearForm() {
        fldId.clear();
        fldName.clear();
        fldProgramme.clear();
        fldGpa.clear();
        fldEmail.clear();
        fldPhone.clear();
        fldLevel.setValue(null);
        fldStatus.setValue("Active");
        lblFormError.setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}