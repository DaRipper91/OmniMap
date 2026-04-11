import React from 'react';
import { useContinuumStore } from '../store';
import { ArrowRightLeft, Trash2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getPlatform } from '../native-bridge-impl';
import { Haptics, ImpactStyle } from '@capacitor/haptics';

export const KanbanBoard: React.FC<{ tasks: any[] }> = ({ tasks }) => {
  const proposeMutation = useContinuumStore(state => state.proposeMutation);
  const deleteTask = useContinuumStore(state => state.deleteTask);
  
  const triggerHaptic = () => {
    if (getPlatform() === 'android') Haptics.impact({ style: ImpactStyle.Medium });
  };

  const moveTask = (task: any, newStatus: string) => {
    triggerHaptic();
    proposeMutation({
      type: 'UPDATE',
      entity: 'TASK',
      payload: { id: task.id, data: { ...task.data, status: newStatus } },
    });
  };

  const columns = [
    { id: 'todo', title: 'To Do', color: '#A09E9F' },
    { id: 'in_progress', title: 'In Progress', color: '#7FBCFF' },
    { id: 'blocked', title: 'Blocked', color: '#FF6464' },
    { id: 'done', title: 'Done', color: '#B4E391' }
  ];

  return (
    <div className="kanban-scroll-container">
      <div className="kanban-board">
        {columns.map(col => (
          <div key={col.id} className="kanban-column">
            <div className="kanban-header">
              <div className="kanban-dot" style={{ background: col.color }} />
              <h3>{col.title}</h3>
              <span className="kanban-count">{tasks.filter(t => (t.data?.status || 'todo') === col.id).length}</span>
            </div>
            <div className="kanban-task-list">
              {tasks.filter(t => (t.data?.status || 'todo') === col.id).map(task => (
                <div key={task.id} className="kanban-card">
                  <div className="kanban-card-header">
                    <h4>{task.title}</h4>
                    <button className="icon-btn-small" onClick={() => deleteTask(task.id)}>
                      <Trash2 size={14} color="#FF6464" />
                    </button>
                  </div>
                  <div className="kanban-markdown">
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>{task.description}</ReactMarkdown>
                  </div>
                  <div className="kanban-actions">
                    {col.id !== 'todo' && <button onClick={() => moveTask(task, 'todo')}>Todo</button>}
                    {col.id !== 'in_progress' && <button onClick={() => moveTask(task, 'in_progress')}>Doing</button>}
                    {col.id !== 'blocked' && <button onClick={() => moveTask(task, 'blocked')}>Block</button>}
                    {col.id !== 'done' && <button onClick={() => moveTask(task, 'done')}>Done</button>}
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
      <style>{`
        .kanban-scroll-container {
          width: 100%;
          overflow-x: auto;
          padding-bottom: 16px;
        }
        .kanban-board {
          display: flex;
          gap: 16px;
          padding: 8px;
          min-width: max-content;
        }
        .kanban-column {
          width: 300px;
          background: var(--secondary-container);
          border-radius: var(--radius-lg);
          padding: 16px;
          display: flex;
          flex-direction: column;
          gap: 16px;
          max-height: 70vh;
        }
        .kanban-header {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .kanban-dot {
          width: 12px;
          height: 12px;
          border-radius: 50%;
        }
        .kanban-header h3 {
          margin: 0;
          font-size: 1.1rem;
          flex: 1;
        }
        .kanban-count {
          background: rgba(255,255,255,0.1);
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 0.8rem;
          font-weight: bold;
        }
        .kanban-task-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
          overflow-y: auto;
        }
        .kanban-card {
          background: var(--surface-variant);
          padding: 16px;
          border-radius: var(--radius-md);
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .kanban-card-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        }
        .kanban-card-header h4 {
          margin: 0;
          font-size: 1rem;
          line-height: 1.3;
        }
        .icon-btn-small {
          background: transparent;
          border: none;
          cursor: pointer;
          opacity: 0.5;
        }
        .icon-btn-small:hover { opacity: 1; }
        .kanban-markdown {
          font-size: 0.85rem;
          color: #E6E1E5;
          opacity: 0.8;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
        }
        .kanban-markdown p { margin: 0 0 4px 0; }
        .kanban-actions {
          display: flex;
          gap: 8px;
          margin-top: 8px;
          flex-wrap: wrap;
        }
        .kanban-actions button {
          background: var(--surface);
          border: 1px solid var(--surface-variant);
          color: var(--primary);
          padding: 4px 8px;
          border-radius: 8px;
          font-size: 0.75rem;
          cursor: pointer;
        }
        .kanban-actions button:active { background: var(--primary-container); }
      `}</style>
    </div>
  );
};
