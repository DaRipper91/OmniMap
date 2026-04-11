import React, { useEffect } from 'react';
import { useContinuumStore } from '../store';
import { motion } from 'framer-motion';
import { RefreshCcw, ExternalLink, Activity, Cloud, Download } from 'lucide-react';

export function NexiumPortalMonitor() {
  const portals = useContinuumStore((state) => state.portals);
  const refreshPortals = useContinuumStore((state) => state.refreshPortals);
  const ingestPortalResults = useContinuumStore((state) => state.ingestPortalResults);
  const aiStatus = useContinuumStore((state) => state.aiStatus);

  useEffect(() => {
    refreshPortals();
  }, []);

  const handleIngest = async (portalId: string) => {
    // Confirmation could be added here
    await ingestPortalResults(portalId);
  };

  return (
    <div className="nexium-monitor">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
        <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Cloud size={20} color="var(--primary)" /> Active Portals
        </h3>
        <button 
          onClick={() => refreshPortals()} 
          disabled={aiStatus === 'thinking'}
          className={`icon-btn ${aiStatus === 'thinking' ? 'spinning' : ''}`}
        >
          <RefreshCcw size={18} />
        </button>
      </div>

      {portals.length === 0 ? (
        <div className="empty-state" style={{ padding: '24px' }}>
          <Activity size={32} opacity={0.3} />
          <p>No active Portals found in the Ghost Nexium.</p>
        </div>
      ) : (
        <div className="portal-list" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {portals.map((portal) => (
            <motion.div 
              key={portal.id}
              whileTap={{ scale: 0.98 }}
              className="portal-card"
            >
              <div className="portal-header">
                <span className="portal-id">#{portal.id.substring(0, 8)}</span>
                <span className={`portal-status status-${portal.status.toLowerCase().replace(/\s+/g, '-')}`}>
                  {portal.status}
                </span>
              </div>
              <p className="portal-desc">{portal.description}</p>
              <div className="portal-footer">
                <span className="portal-repo">{portal.repo}</span>
                <div style={{ display: 'flex', gap: '12px' }}>
                  {portal.status.toLowerCase().includes('awaiting user feedback') && (
                    <button 
                      onClick={() => handleIngest(portal.id)}
                      className="portal-action-btn"
                      disabled={aiStatus === 'thinking'}
                    >
                      <Download size={14} /> Ingest
                    </button>
                  )}
                  <a 
                    href={`https://jules.google/session/${portal.id}`} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="portal-link"
                  >
                    View <ExternalLink size={14} />
                  </a>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      <style>{`
        .portal-card {
          background: var(--surface-variant);
          border-radius: 16px;
          padding: 16px;
          border: 1px solid rgba(255,255,255,0.05);
        }

        .portal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
        }

        .portal-id {
          font-family: monospace;
          font-size: 0.8rem;
          opacity: 0.5;
        }

        .portal-status {
          font-size: 0.75rem;
          font-weight: bold;
          padding: 2px 8px;
          border-radius: 100px;
          background: rgba(255,255,255,0.1);
        }

        .status-awaiting-user-feedback { background: rgba(208, 188, 255, 0.2); color: var(--primary); }
        .status-thinking { background: rgba(127, 188, 255, 0.2); color: #7fbcff; }
        .status-completed { background: rgba(180, 227, 145, 0.2); color: #b4e391; }

        .portal-desc {
          margin: 0 0 12px 0;
          font-size: 0.95rem;
          line-height: 1.4;
        }

        .portal-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 0.8rem;
        }

        .portal-repo { opacity: 0.6; }

        .portal-link {
          color: var(--primary);
          text-decoration: none;
          display: flex;
          align-items: center;
          gap: 4px;
          font-weight: bold;
        }

        .portal-action-btn {
          background: var(--primary-container);
          color: var(--primary);
          border: none;
          border-radius: 8px;
          padding: 4px 8px;
          font-size: 0.75rem;
          font-weight: bold;
          display: flex;
          align-items: center;
          gap: 4px;
          cursor: pointer;
        }

        .portal-action-btn:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .spinning { animation: spin 1s linear infinite; }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
