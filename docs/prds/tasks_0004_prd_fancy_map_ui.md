# Technical Plan: The Fancy Map UI

## Overview
Implement a spatially-aware Node-Graph visualization component. Use `framer-motion` for fluid, spring-based transitions and SVG for crisp, styleable rendering of nodes and edges.

## PRD Reference
- File: `docs/prds/0004_prd_fancy_map_ui.md`
- Feature: Fancy Map UI

## Relevant Files

### Files to Modify
- `package.json` - Add `framer-motion`.
- `src/App.tsx` - Add "Map" view toggle and integrate the new component.
- `src/store.ts` - Add `graphLayout` state to persist node positions (optional).

### Files to Create
- `src/components/MindMap.tsx` - The core graph visualization component.
- `src/components/NodeElement.tsx` - Individual interactive node component.
- `src/layout-engine.ts` - Logic for tree/force positioning.

---

## Phase 1: Infrastructure & Scaffolding

Add animation libraries and create the component shell.

### Tasks
- [ ] 1.0 **Dependencies**
  - [ ] 1.1 Install `framer-motion`.
- [ ] 2.0 **View Switcher**
  - [ ] 2.1 Add `viewMode` state to `App.tsx` (list vs map).
  - [ ] 2.2 Create a basic `MindMap.tsx` that renders a full-screen SVG.

### Verification Criteria
- [ ] User can toggle between Dashboard and Map view.
- [ ] Map view shows a blank SVG canvas that pans and zooms.

---

## Phase 2: Graph Rendering

Implement the logic to draw nodes and edges.

### Tasks
- [ ] 3.0 **Node Rendering**
  - [ ] 3.1 Implement `NodeElement.tsx` using `motion.g`.
  - [ ] 3.2 Add "Neon Glow" styling based on node type.
- [ ] 4.0 **Edge Rendering**
  - [ ] 4.1 Implement curved SVG paths for edges.
  - [ ] 4.2 Sync edge positions with node movement.

### Verification Criteria
- [ ] Every node in the store appears as a distinct visual element.
- [ ] Edges correctly connect parents to children.

---

## Phase 3: Fluid Interaction & Layout

Add animations and spatial navigation.

### Tasks
- [ ] 5.0 **Liquid Layout**
  - [ ] 5.1 Implement a simple tree-hierarchy layout in `src/layout-engine.ts`.
  - [ ] 5.2 Add spring transitions when switching projects or expanding nodes.
- [ ] 6.0 **Direct Manipulation**
  - [ ] 6.1 Enable dragging nodes to move them.
  - [ ] 6.2 Add click-to-focus behavior.

### Verification Criteria
- [ ] Node positions update smoothly when the project context changes.
- [ ] Panning and zooming feels "Pro" (120Hz smooth).

---

## Phase 4: Review & Audit

### Tasks
- [ ] 7.0 **Agent 7: Technical Review**
  - [ ] 7.1 Audit render performance (prevent unnecessary re-renders).
  - [ ] 7.2 Verify SVG coordinate math.
- [ ] 8.0 **Agent 8: QA Audit**
  - [ ] 8.1 20-point audit of visual polish and accessibility.
  - [ ] 8.2 Final [YES/NO] Report for Part 4.

---

## Risks & Mitigations
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| SVG Performance with 100+ nodes | Med | Med | Use `requestAnimationFrame` for edge updates or switch to Canvas if threshold hit. |
| Overlapping nodes | High | Low | Implement basic collision avoidance in the layout engine. |

## Estimated Effort
- **Complexity:** HIGH (Requires advanced React animation and geometry knowledge).
