import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Search, FileText, CheckSquare, Folder } from 'lucide-react';
import { useContinuumStore } from '../store';
import { getPlatform } from '../native-bridge-impl';
import { Haptics, ImpactStyle } from '@capacitor/haptics';

export const GlobalSearch: React.FC<{ isOpen: boolean; onClose: () => void }> = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const projects = useContinuumStore(state => state.projects);
  const tasks = useContinuumStore(state => state.tasks);
  const notes = useContinuumStore(state => state.notes);
  const setActiveProject = useContinuumStore(state => state.setActiveProject);

  const triggerHaptic = () => {
    if (getPlatform() === 'android') Haptics.impact({ style: ImpactStyle.Light });
  };

  const results = [
    ...projects.filter(p => p.name.toLowerCase().includes(query.toLowerCase())).map(p => ({ type: 'project', item: p })),
    ...tasks.filter(t => t.title.toLowerCase().includes(query.toLowerCase()) || t.description.toLowerCase().includes(query.toLowerCase())).map(t => ({ type: 'task', item: t })),
    ...notes.filter(n => n.content.toLowerCase().includes(query.toLowerCase())).map(n => ({ type: 'note', item: n }))
  ].slice(0, 15);

  const handleSelect = (result: any) => {
    triggerHaptic();
    if (result.type === 'project') {
      setActiveProject(result.item.id);
    } else if (result.item.projectId) {
      setActiveProject(result.item.projectId);
    }
    onClose();
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="search-overlay">
          <motion.div 
            className="search-modal"
            initial={{ opacity: 0, scale: 0.95, y: -20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: -20 }}
          >
            <div className="search-input-wrapper">
              <Search size={20} color="var(--primary)" />
              <input 
                autoFocus
                type="text" 
                placeholder="Search projects, tasks, notes..." 
                value={query}
                onChange={e => setQuery(e.target.value)}
              />
              <button className="icon-btn" onClick={onClose}><X size={20} /></button>
            </div>

            <div className="search-results">
              {query && results.length === 0 && (
                <div className="empty-results">No results found for "{query}"</div>
              )}
              {query && results.map((res, i) => (
                <div key={i} className="search-result-item" onClick={() => handleSelect(res)}>
                  <div className="result-icon">
                    {res.type === 'project' && <Folder size={16} />}
                    {res.type === 'task' && <CheckSquare size={16} />}
                    {res.type === 'note' && <FileText size={16} />}
                  </div>
                  <div className="result-content">
                    <h4>{res.type === 'project' ? (res.item as any).name : (res.type === 'task' ? (res.item as any).title : 'Note')}</h4>
                    <p>{res.type === 'project' ? (res.item as any).description : (res.type === 'task' ? (res.item as any).description : (res.item as any).content)}</p>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>

          <style>{`
            .search-overlay {
              position: fixed;
              inset: 0;
              background: rgba(0,0,0,0.6);
              backdrop-filter: blur(4px);
              z-index: 2000;
              display: flex;
              justify-content: center;
              padding: 24px 16px;
            }
            .search-modal {
              background: var(--surface);
              width: 100%;
              max-width: 600px;
              max-height: 80vh;
              border-radius: var(--radius-lg);
              display: flex;
              flex-direction: column;
              overflow: hidden;
              box-shadow: 0 10px 40px rgba(0,0,0,0.5);
              border: 1px solid var(--surface-variant);
            }
            .search-input-wrapper {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 16px 20px;
              border-bottom: 1px solid var(--surface-variant);
              background: var(--secondary-container);
            }
            .search-input-wrapper input {
              flex: 1;
              background: transparent;
              border: none;
              color: var(--on-surface);
              font-size: 1.1rem;
              outline: none;
            }
            .search-results {
              flex: 1;
              overflow-y: auto;
              padding: 12px;
              display: flex;
              flex-direction: column;
              gap: 8px;
            }
            .search-result-item {
              display: flex;
              align-items: center;
              gap: 16px;
              padding: 12px 16px;
              background: var(--surface-variant);
              border-radius: var(--radius-md);
              cursor: pointer;
            }
            .search-result-item:active {
              background: var(--primary-container);
            }
            .result-icon {
              background: rgba(255,255,255,0.1);
              padding: 8px;
              border-radius: 50%;
              display: flex;
              color: var(--primary);
            }
            .result-content h4 {
              margin: 0 0 4px 0;
              font-size: 1rem;
            }
            .result-content p {
              margin: 0;
              font-size: 0.85rem;
              color: #A09E9F;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 250px;
            }
            .empty-results {
              text-align: center;
              padding: 32px;
              color: #A09E9F;
            }
          `}</style>
        </div>
      )}
    </AnimatePresence>
  );
};
