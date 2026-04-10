import React from 'react';
import { motion } from 'framer-motion';
import { type Node } from '../types';

interface NodeElementProps {
  node: Node;
  x: number;
  y: number;
  isSelected: boolean;
  onSelect: (id: string) => void;
}

export const NodeElement: React.FC<NodeElementProps> = ({ node, x, y, isSelected, onSelect }) => {
  const colorMap: Record<string, string> = {
    PROJECT: '#4f46e5',
    TASK: '#3b82f6',
    IDEA: '#10b981',
    GOAL: '#f59e0b'
  };

  return (
    <motion.g
      initial={false}
      animate={{ x, y }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
      style={{ cursor: 'pointer' }}
      onClick={(e) => { e.stopPropagation(); onSelect(node.id); }}
    >
      <motion.rect
        width={180}
        height={60}
        rx={10}
        fill="#16191e"
        stroke={isSelected ? '#fff' : colorMap[node.type] || '#64748b'}
        strokeWidth={isSelected ? 3 : 2}
        initial={false}
        animate={{ filter: isSelected ? 'drop-shadow(0 0 8px #4f46e5)' : 'none' }}
      />
      <text
        x={90}
        y={25}
        textAnchor="middle"
        fill="#e2e8f0"
        style={{ fontSize: '12px', fontWeight: 'bold', pointerEvents: 'none' }}
      >
        {node.title}
      </text>
      <text
        x={90}
        y={45}
        textAnchor="middle"
        fill="#94a3b8"
        style={{ fontSize: '10px', pointerEvents: 'none' }}
      >
        {node.type}
      </text>
    </motion.g>
  );
};
