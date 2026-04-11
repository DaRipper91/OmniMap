import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import React, { useMemo } from 'react';
import { motion } from 'framer-motion';
import { useContinuumStore } from '../store';
import { calculateTreeLayout } from '../layout-engine';
import { NodeElement } from './NodeElement';
export const MindMap = () => {
    const nodes = useContinuumStore((state) => state.nodes);
    const edges = useContinuumStore((state) => state.edges);
    const activeProjectId = useContinuumStore((state) => state.activeProjectId);
    const [selectedNodeId, setSelectedNodeId] = React.useState(null);
    const positions = useMemo(() => {
        return calculateTreeLayout({ nodes, edges }, activeProjectId);
    }, [nodes, edges, activeProjectId]);
    return (_jsx("div", { className: "mind-map-container", style: { width: '100%', height: '100%', overflow: 'hidden', background: '#0a0c0f' }, children: _jsxs(motion.svg, { width: "100%", height: "100%", viewBox: "0 0 2000 2000", drag: true, dragConstraints: { left: -1000, right: 1000, top: -1000, bottom: 1000 }, children: [edges.map((edge) => {
                    const start = positions[edge.sourceId];
                    const end = positions[edge.targetId];
                    if (!start || !end)
                        return null;
                    return (_jsx(motion.path, { d: `M ${start.x + 180} ${start.y + 30} C ${start.x + 240} ${start.y + 30}, ${end.x - 60} ${end.y + 30}, ${end.x} ${end.y + 30}`, fill: "none", stroke: "#1e293b", strokeWidth: 2, initial: { pathLength: 0, opacity: 0 }, animate: { pathLength: 1, opacity: 1 }, transition: { duration: 0.5 } }, edge.id));
                }), nodes.map((node) => {
                    const pos = positions[node.id];
                    if (!pos)
                        return null;
                    return (_jsx(NodeElement, { node: node, x: pos.x, y: pos.y, isSelected: selectedNodeId === node.id, onSelect: setSelectedNodeId }, node.id));
                })] }) }));
};
//# sourceMappingURL=MindMap.js.map