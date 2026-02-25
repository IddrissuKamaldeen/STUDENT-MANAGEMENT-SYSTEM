# Changelog

## [1.0.0] - 2026-02-23

### Added
- Initial project setup with Maven, JavaFX 21, SQLite JDBC
- Student domain model with all required fields
- SQLite repository with full CRUD using prepared statements
- Validation service enforcing all field rules from the brief
- Student service with business logic and report calculations
- Dashboard screen with live stats (total, active, inactive, avg GPA)
- Students screen with table, search, filter, sort, add, edit, delete
- Reports screen with 4 tabs: Top Performers, At-Risk, GPA Distribution, Programme Summary
- Import screen with CSV parsing, validation, duplicate detection, background thread
- Export screen: all students, top performers, at-risk to CSV in data folder
- Settings screen for configurable at-risk GPA threshold
- File-based logger writing to data/app.log
- 20 unit tests covering validation, reports, and service logic
- README with Windows run instructions
- RUN_VM_OPTIONS.txt with JavaFX VM arguments
