# PRD: Node Graph Data Model (Project Singularity)

## 1. Project Overview
Continuum is evolving from a flat list-based manager into a **Node & Edge Graph**. This PRD defines the transition where every entity (Project, Task, Note, Goal) is a unified "Node," and relationships are represented as "Edges."

## 2. Problem Statement
Flat lists are too rigid for AI-assisted synthesis. To allow AI to restructure, cluster, and link disparate ideas into actionable projects, we need a data structure that supports arbitrary nesting and multi-parent relationships.

## 3. Goals
- **Unified Schema:** Everything is a Node.
- **Relational Integrity:** Use Edges to define `parent-of`, `depends-on`, and `related-to`.
- **Backward Compatibility:** Map existing Projects/Tasks/Notes into the new graph model.
- **AI-Ready:** Enable agents to manipulate the graph (re-parenting, splitting).

## 4. Non-Goals (Out of Scope)
- Visualizing the graph as a force-directed map (Task 6.x / Phase 7+).
- Collaborative multi-user graph editing.

## 5. User Stories
As a developer, I want to capture a raw thought and later have the AI link it as a "Sub-task" of a project or a "Reference" for a goal without manually moving data.

## 6. Functional Requirements

### Must Have (P0)
- [ ] **Node Interface:** Standardized properties (`id`, `type`, `data`, `metadata`).
- [ ] **Edge Interface:** Standardized link structure (`sourceId`, `targetId`, `type`).
- [ ] **Graph Store:** Updated Zustand store to manage a `nodes[]` and `edges[]` array.
- [ ] **Materialized Views:** Logic to render the graph as the current List View.

### Should Have (P1)
- [ ] **Edge Validation:** Prevent circular dependencies for `parent-of` relationships.
- [ ] **Bulk Mutations:** AI ability to propose moving whole sub-graphs.

## 7. Business Invariants
- A Node must always have a `type` (IDEA, TASK, GOAL, PROJECT).
- An Edge must always have valid `sourceId` and `targetId`.

## 8. Failure States
| Failure Scenario | Expected Behavior |
|------------------|-------------------|
| Orphaned Edge | If a node is deleted, associated edges must be cleaned up. |
| Circular Parent | UI/Store must block setting a parent that is also a child. |

## 9. Technical Considerations
- **Data Migration:** A script to convert the existing `projects[]`, `tasks[]`, and `notes[]` into `nodes[]`.
- **Query Performance:** Basic indexing by `type` and `id` to keep the UI snappy.

## 10. Success Metrics
- 100% of existing data migrated to the graph model.
- AI Agents can successfully propose a `RE-PARENT` mutation.
