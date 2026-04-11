import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import React from 'react';
import { motion } from 'framer-motion';
import {} from '../types';
export const NodeElement = ({ node, x, y, isSelected, onSelect }) => {
    const colorMap = {
        PROJECT: '#4f46e5',
        TASK: '#3b82f6',
        IDEA: '#10b981',
        GOAL: '#f59e0b'
    };
    return (_jsxs(motion.g, { initial: false, animate: { x, y }, transition: { type: 'spring', stiffness: 300, damping: 30 }, style: { cursor: 'pointer' }, onClick: (e) => { e.stopPropagation(); onSelect(node.id); }, children: [_jsx(motion.rect, { width: 180, height: 60, rx: 10, fill: "#16191e", stroke: isSelected ? '#fff' : colorMap[node.type] || '#64748b', strokeWidth: isSelected ? 3 : 2, initial: false, animate: { filter: isSelected ? 'drop-shadow(0 0 8px #4f46e5)' : 'none' } }), _jsx("text", { x: 90, y: 25, textAnchor: "middle", fill: "#e2e8f0", style: { fontSize: '12px', fontWeight: 'bold', pointerEvents: 'none' }, children: node.title }), _jsx("text", { x: 90, y: 45, textAnchor: "middle", fill: "#94a3b8", style: { fontSize: '10px', pointerEvents: 'none' }, children: node.type })] }));
};
//# sourceMappingURL=NodeElement.js.map