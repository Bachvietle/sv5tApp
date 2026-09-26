# AGENTS.md — SinhVien5T Backend

## 1. Project Overview

SinhVien5T is a Spring Boot backend for a platform supporting the "Sinh viên 5 Tốt" student recognition process.

The goal of the system is not only to provide a place for students to submit applications and wait for evaluation. It aims to create an environment where Students, Mentors, and Youth Union Officers/Admins can interact and support the entire journey toward achieving the Sinh viên 5 Tốt recognition.

### Main actors

- **Student**: Manage personal information, prepare an application, upload evidence, track evaluation progress, receive feedback, and improve their application.
- **Mentor**: Support students, answer questions, provide guidance, and potentially assist with evidence/application review.
- **Admin / Youth Union Officer**: Manage the evaluation process, review applications and evidence, provide feedback, manage relevant activities/content, and support organizational operations.

### Core business direction

The system should gradually support:


Student
   ↓
Prepare application
   ↓
Submit evidence
   ↓
Admin / Mentor review
   ↓
Feedback / Correction
   ↓
Re-submit
   ↓
Re-evaluation
   ↓
Final evaluation

The five criteria and evaluation rules are business-specific. Do not invent or assume rules when the existing requirements or implementation are unclear.

Current development status

The basic Student-side application submission flow has already been implemented.

The current development focus is the Admin / Mentor side, especially:

Viewing submitted applications
Reviewing student evidence
Evaluating the five criteria
Providing feedback
Approving or rejecting evidence
Managing application status and evaluation workflow

Other features such as mentoring, community, chat, notifications, activities, personalized planning, ranking, and analytics are future directions unless explicitly requested.

2. Tech Stack

The backend is primarily built with:

Java
Spring Boot
Spring Web / REST API
Spring Data JPA / Hibernate
Maven
PostgreSQL or the database configured by the project
Docker where applicable
Lombok where already used

Follow the versions and dependencies already defined in the project.

Before introducing a new library, framework, or architectural approach, check whether the project already has an appropriate solution.

3. Repository Structure & Architecture

Follow the existing project structure and architecture unless there is a clear technical reason to change it.

The backend generally follows a layered structure:

Controller
    ↓
Service
    ↓
Repository
    ↓
Database

Typical responsibilities:

Controller — HTTP/API layer
Service — business logic and workflows
Repository — persistence and database access
Entity — domain/persistence model
DTO — API request/response models where applicable
Security — authentication and authorization
Exception handling — centralized error handling where applicable

Before creating a new class, package, utility, or abstraction:

Search the codebase for existing solutions.
Follow the established conventions when they are appropriate.
Prefer reuse over unnecessary duplication.

However, do not blindly follow existing code.

If the current implementation contains a bug, poor design, inconsistency, security issue, or technical limitation, identify it and propose a better solution when appropriate.

The goal is to maintain consistency without preserving bad patterns.

4. Coding Style & Conventions

Follow the existing project's coding style and conventions.

Prefer:

Clear and meaningful names
Small and focused methods
Single responsibility
Constructor-based dependency injection
Reusable components when there is a genuine need
Consistent DTO, Entity, Service, Repository, and Controller patterns
Readable and maintainable code

Avoid:

Unnecessary abstractions
Over-engineering
Duplicated business logic
Large unrelated refactoring
Introducing new patterns only for stylistic reasons

When existing code is clearly problematic, do not preserve it merely for consistency. Use engineering judgment and improve it when the change is relevant to the task.

5. Development Workflow

For non-trivial tasks, follow this general workflow:

Understand requirement
        ↓
Inspect existing code
        ↓
Understand affected workflow
        ↓
Plan solution
        ↓
Implement
        ↓
Test / Verify
        ↓
Review changes

For small and straightforward tasks, the process can be simplified.

Before implementing a significant feature, inspect related:

Controllers
Services
Repositories
Entities
DTOs
Security
Tests
Configuration

Do not modify unrelated parts of the project.

6. Agent Behavior

Act as an engineering partner, not a simple code generator.

Understand before changing

Do not immediately start coding a feature without first understanding the relevant existing implementation.

Search and inspect the codebase when necessary.

Preserve context

Respect:

Existing architecture
Existing business logic
Existing naming conventions
Existing dependencies
Existing API patterns
Existing database relationships

But do not treat existing code as unquestionably correct.

Use engineering judgment

If you discover:

A bug
A security problem
A broken design
An incorrect assumption
A missing edge case
An existing implementation that conflicts with the requirement

point it out and recommend an appropriate solution.

You are allowed to improve existing code when it is necessary for correctness, maintainability, security, or the requested feature.

Do not sacrifice correctness merely to preserve an existing pattern.

Scope control

Do not:

Implement features that were not requested
Make unrelated refactoring
Delete code without understanding its usage
Change database structure casually
Change authentication/security behavior without considering its impact
Commit or push changes unless explicitly requested
When requirements are ambiguous

Do not invent important business rules.

Explain the ambiguity, make a reasonable recommendation when possible, and ask for clarification when the decision materially affects the system.

Verification

After making changes:

Run relevant tests or build commands.
Check for compilation errors.
Review the resulting diff.
Report what was changed and what was verified.
Clearly report anything that could not be verified.
7. Git & Safety

Keep changes focused and reviewable.

Before making significant changes, check the current Git state.

Never discard existing user changes.

Do not automatically:

Commit
Push
Force push
Reset or clean the repository destructively
Perform destructive database operations

Always preserve the user's work unless explicitly instructed otherwise.

8. Important Rule

The purpose of this file is to provide context and guardrails, not rigid instructions for every implementation detail.

The Agent should:

Understand the project → follow good existing conventions → use independent engineering judgment → improve problems when necessary → verify the result.

Do not follow this document mechanically when doing so would produce incorrect, fragile, or unnecessarily complex code.