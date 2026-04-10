# Technical Plan: Node Graph Data Model

## Overview
Transition the Continuum store from specialized arrays (`projects`, `tasks`, `notes`) into a unified **Graph Model** using `nodes` and `edges`. This provides the flexibility needed for AI-driven restructuring and multi-parent relationships.

## PRD Reference
- File: `docs/prds/0001_prd_node_graph_model.md`
- Feature: Node Graph Data Model

## Relevant Files

### Files to Modify
- `src/types.ts` - Define unified `Node` and `Edge` interfaces.
- `src/store.ts` - Update Zustand store to manage graph state and implement migration logic.
- `src/App.tsx` - Update component logic to consume the new graph structure via materialized views.

### Files to Create
- `src/graph-utils.ts` - Helper functions for graph traversal, filtering, and validation.

## Key Algorithms

### 1. Materialized View Generator
**Purpose:** Reconstruct the hierarchical List View (Project > Task) from the flat `nodes` and `edges` arrays.
**Steps:**
1. Filter nodes by type (e.g., `PROJECT`).
2. For each project node, find edges where `sourceId` is the project ID and type is `parent-of`.
3. Resolve target nodes from those edges to populate children.

### 2. Migration Script
**Purpose:** Ensure zero data loss during the transition.
**Steps:**
1. Read current state (`projects`, `tasks`, `notes`).
2. Map each entity to a `Node` with a corresponding type.
3. Create `edges` based on existing `projectId` references.
4. Clear old arrays and commit to the new store structure.

---

## AI Guardrails Applied

### Type Safety
- [ ] Implement strict `Node` type discriminator union in `src/types.ts`.
- [ ] Add runtime validation for Edge source/target existence.

### Error Handling
- [ ] Handle orphaned edges during node deletion.
- [ ] Prevent circular dependencies in `parent-of` relationships.

---

## Phase 1: Data Layer & Migration

Transition core types and implement the migration logic.

### Tasks
- [ ] 1.0 **Define Unified Graph Types**
  - [ ] 1.1 Update `src/types.ts` with `Node`, `NodeType`, and `Edge` interfaces.
  - [ ] 1.2 Update `ContinuumState` to include `nodes: Node[]` and `edges: Edge[]`.
- [ ] 2.0 **Implement Migration Engine**
  - [ ] 2.1 Create `migrateToGraph` function in `src/store.ts`.
  - [ ] 2.2 Trigger migration on store initialization if legacy arrays exist.

### Verification Criteria
- [ ] Types compile with no errors.
- [ ] Existing mock data from `main.tsx` correctly appears in the `nodes` array.

---

## Phase 2: Store Logic & Utils

Update actions to manipulate the graph and add traversal utilities.

### Tasks
- [ ] 3.0 **Graph CRUD Actions**
  - [ ] 3.1 Implement `addNode`, `updateNode`, `deleteNode` in `src/store.ts`.
  - [ ] 3.2 Implement `addEdge`, `removeEdge`.
- [ ] 4.0 **Traversal Utilities**
  - [ ] 4.1 Create `src/graph-utils.ts`.
  - [ ] 4.2 Implement `getChildren(nodeId)` and `getParents(nodeId)`.

### Verification Criteria
- [ ] Deleting a node automatically removes its associated edges.
- [ ] Adding an edge to a non-existent node throws a descriptive error.

---

## Phase 3: UI Integration

Update the React dashboard to work with the graph model.

### Tasks
- [ ] 5.0 **Update Dashboard Views**
  - [ ] 5.1 Update Sidebar to resolve projects from `nodes.filter(n => n.type === 'PROJECT')`.
  - [ ] 5.2 Update Task Grid to resolve tasks via edges related to the active project node.
- [ ] 6.0 **Refactor Mutation Pipeline**
  - [ ] 6.1 Update `proposeMutation` to support `NODE` and `EDGE` entities.

### Verification Criteria
- [ ] The UI remains visually identical to Phase 6 but operates on graph data.
- [ ] Switching projects correctly filters nodes via edges.

---

## Phase 4: Review & Audit (Final Boss)

### Tasks
- [ ] 7.0 **Agent 7: Technical Review**
  - [ ] 7.1 Audit logic for circular dependencies.
  - [ ] 7.2 Verify data integrity post-migration.
- [ ] 8.0 **Agent 8: QA Audit**
  - [ ] 8.1 Performance check on Pixel 9/10 Pro (simulated).
  - [ ] 8.2 Final [YES/NO] Report generation.

---

## Risks & Mitigations
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Data Corruption during migration | Med | High | Preserve legacy arrays in `localStorage` until migration is verified. |
| Performance degradation on large graphs | Low | Med | Implement memoized selectors for graph traversals. |

## Estimated Effort
| Phase | Complexity |
|-------|------------|
| Phase 1 | Med |
| Phase 2 | Med |
| Phase 3 | Med |
| Phase 4 | High |
| **Total** | **Atomic Implementation** |
