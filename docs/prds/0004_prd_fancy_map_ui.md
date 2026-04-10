# PRD: The Fancy Map UI (Project Singularity)

## 1. Project Overview
Implement a high-performance, visually stunning **Node-Graph Visualization (Mind Map)** to complement the existing dashboard. This UI will leverage the graph data model (Part 1) to render nodes and edges in a spatial layout with liquid-smooth animations.

## 2. Problem Statement
The current List View is efficient but lacks the "Mind Map" intuition needed for complex planning and idea clustering. To truly eliminate context switching, users need to see the relationships between ideas and tasks spatially.

## 3. Goals
- **Liquid Fluidity:** 120Hz optimized animations using `framer-motion`.
- **Spatial Navigation:** Pan and zoom capabilities for exploring large graphs.
- **Node Interaction:** Direct interaction (click to expand, drag to re-parent) within the map.
- **Fancy Aesthetic:** Centered "Mind Map" view with neon-glow nodes and connected edges.

## 4. Functional Requirements

### Must Have (P0)
- [ ] **Graph Renderer:** A React component that draws Nodes and Edges based on the Store.
- [ ] **Fluid Motion:** Spring-based animations for node transitions and layout changes.
- [ ] **Switchable Views:** Ability to toggle between "Dashboard" (List) and "Map" (Spatial) modes.

### Should Have (P1)
- [ ] **Auto-Layout:** A simple force-directed or tree-based algorithm to position nodes.
- [ ] **Interactive Edges:** Visual cues for `PARENT_OF` vs `RELATED_TO` relationships.

## 5. UX/Design Considerations
- **Pixel 9/10 Pro focus:** Utilize high refresh rates.
- **Dark Mode only:** Deep blacks with vibrant accent colors for nodes.
- **Gesture Support:** Touch-friendly pan and zoom.

## 6. Technical Considerations
- **framer-motion** for animations.
- **Canvas or SVG?** SVG for smaller graphs (easier to style), Canvas for large clusters. Start with SVG.

## 7. Success Metrics
- Users can switch to "Map Mode" and see their entire "Continuum Core" project as a connected tree.
- Animation frame rate remains stable at 60-120fps during pans.
