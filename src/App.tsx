import React from 'react';
import { useContinuumStore } from './store';
import { getChildren } from './graph-utils';

import { nativeFS, getPlatform } from './native-bridge-impl';

import { MindMap } from './components/MindMap';

function App() {
  const nodes = useContinuumStore((state) => state.nodes);
  const edges = useContinuumStore((state) => state.edges);
  const models = useContinuumStore((state) => state.models);
  const runtimes = useContinuumStore((state) => state.runtimes);
  const downloadProgress = useContinuumStore((state) => state.downloadProgress);
  const requestAI = useContinuumStore((state) => state.requestAI);
  const downloadModel = useContinuumStore((state) => state.downloadModel);
  const suggestedActions = useContinuumStore((state) => state.suggestedActions);
  const undoHistory = useContinuumStore((state) => state.undoHistory);
  const activeProjectId = useContinuumStore((state) => state.activeProjectId);
  const setActiveProject = useContinuumStore((state) => state.setActiveProject);
  const proposeMutation = useContinuumStore((state) => state.proposeMutation);
  const commitMutation = useContinuumStore((state) => state.commitMutation);
  const addNote = useContinuumStore((state) => state.addNote);
  const acceptAction = useContinuumStore((state) => state.acceptAction);
  const snoozeAction = useContinuumStore((state) => state.snoozeAction);
  const archiveAction = useContinuumStore((state) => state.archiveAction);
  const undo = useContinuumStore((state) => state.undo);
  const pendingMutations = useContinuumStore((state) => state.pendingMutations);

  const [activeTab, setActiveTab] = React.useState<'dashboard' | 'models'>('dashboard');
  const [viewMode, setViewMode] = React.useState<'list' | 'map'>('list');
  const [platform] = React.useState(getPlatform());

  React.useEffect(() => {
    if (platform === 'android') {
      nativeFS.requestPermissions().then(granted => {
        console.log('Storage permissions:', granted ? 'GRANTED' : 'DENIED');
      });
    }
  }, [platform]);

  const handleSyncVault = async () => {
    if (!activeProject) return;
    console.log('Syncing vault to native storage...');
    // In a real implementation, loop nodes and call nativeFS.writeNode
  };

  // Materialized Views from Graph
  const projects = nodes.filter(n => n.type === 'PROJECT');
  const activeProject = nodes.find(p => p.id === activeProjectId && p.type === 'PROJECT');
  
  // Resolve tasks from edges if project is active, otherwise show all task nodes
  const filteredTasks = activeProjectId 
    ? getChildren({ nodes, edges } as any, activeProjectId).filter(n => n.type === 'TASK')
    : nodes.filter(n => n.type === 'TASK');

  // Resolve notes from graph
  const filteredNotes = activeProjectId
    ? getChildren({ nodes, edges } as any, activeProjectId).filter(n => n.type === 'IDEA')
    : nodes.filter(n => n.type === 'IDEA');

  // Filter suggested actions
  const filteredActions = activeProjectId
    ? suggestedActions.filter(a => a.projectId === activeProjectId && a.status === 'active')
    : suggestedActions.filter(a => a.status === 'active');

  const handleToggleStatus = (task: any) => {
    const currentStatus = task.data?.status || 'todo';
    const nextStatus = currentStatus === 'todo' ? 'in_progress' : (currentStatus === 'in_progress' ? 'done' : 'todo');
    
    proposeMutation({
      type: 'UPDATE',
      entity: 'TASK',
      payload: { id: task.id, data: { ...task.data, status: nextStatus } },
    });
  };

  const handleQuickCapture = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const content = formData.get('note-content') as string;
    
    if (!content.trim()) return;

    addNote({
      id: Math.random().toString(36).substring(2, 9),
      projectId: activeProjectId || undefined,
      content,
      createdAt: Date.now(),
      updatedAt: Date.now(),
      tags: ['quick-capture'],
    });

    e.currentTarget.reset();
  };

  return (
    <div className="app-container">
      <aside className="sidebar">
        <header className="sidebar-header">
          <div className="title-row">
            <h1>Continuum</h1>
            <span className={`platform-badge platform-${platform}`}>{platform}</span>
          </div>
          <div className="status-row">
            <span className="status-dot"></span>
            {platform === 'android' && (
              <button onClick={handleSyncVault} className="btn-sync-mini">Sync Vault</button>
            )}
          </div>
        </header>
        
        <nav className="main-nav">
          <button 
            className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            📊 Dashboard
          </button>
          <button 
            className={`nav-item ${activeTab === 'models' ? 'active' : ''}`}
            onClick={() => setActiveTab('models')}
          >
            🧠 AI Models
          </button>
        </nav>

        <nav className="project-list">
          <div className="section-label">Projects</div>
          {projects.map((project) => (
            <button
              key={project.id}
              className={`project-item ${activeProjectId === project.id ? 'active' : ''}`}
              onClick={() => { setActiveProject(project.id); setActiveTab('dashboard'); }}
            >
              <span className="project-icon">#</span>
              <span className="project-name">{project.title}</span>
            </button>
          ))}
          <button 
            className={`project-item ${activeProjectId === null ? 'active' : ''}`}
            onClick={() => { setActiveProject(null); setActiveTab('dashboard'); }}
          >
            <span className="project-icon">🌍</span>
            <span className="project-name">Global Context</span>
          </button>
        </nav>

        {filteredActions.length > 0 && (
          <div className="suggested-actions-sidebar">
            <div className="section-label">AI Suggestions</div>
            <div className="actions-mini-list">
              {filteredActions.map(action => (
                <div key={action.id} className="action-mini-card">
                  <div className="action-mini-title">{action.title}</div>
                  <div className="action-mini-btns">
                    <button onClick={() => acceptAction(action.id)} className="btn-mini btn-accept">✓</button>
                    <button onClick={() => snoozeAction(action.id, 60)} className="btn-mini">🕒</button>
                    <button onClick={() => archiveAction(action.id)} className="btn-mini">✕</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </aside>

      <main className="main-content">
        <header className="content-header">
          <div className="breadcrumb">
            {activeProject ? (
              <>
                <span className="folder">projects</span>
                <span className="separator">/</span>
                <span className="current">{activeProject.title}</span>
              </>
            ) : (
              <span className="current">{activeTab === 'models' ? 'AI Management' : 'Global View'}</span>
            )}
          </div>
          <div className="header-actions">
            <div className="view-toggle">
              <button 
                className={`btn-toggle ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => setViewMode('list')}
              >
                List
              </button>
              <button 
                className={`btn-toggle ${viewMode === 'map' ? 'active' : ''}`}
                onClick={() => setViewMode('map')}
              >
                Map
              </button>
            </div>
            <button className="btn-icon">🔍</button>
            <button className="btn-icon">⚙️</button>
          </div>
        </header>

        <section className="dashboard" style={{ padding: viewMode === 'map' ? '0' : '40px' }}>
          {activeTab === 'dashboard' ? (
            viewMode === 'map' ? (
              <MindMap />
            ) : (
              <>
                <div className="dashboard-header">
                {activeProject ? (
                  <div className="project-detail">
                    <h2>{activeProject.title}</h2>
                    <p className="description">{activeProject.description}</p>
                    <div className="metadata">
                      <span className="path">{activeProject.data?.path}</span>
                    </div>
                  </div>
                ) : (
                  <div className="global-header">
                    <h2>All Tasks</h2>
                    <p className="description">Unified view across all projects and personal items.</p>
                  </div>
                )}
              </div>

              {pendingMutations.length > 0 && (
                <div className="pending-mutations-section">
                  <div className="section-header">
                    <h3>Approvals Required</h3>
                    <span className="count">{pendingMutations.length}</span>
                  </div>
                  <div className="mutation-list">
                    {pendingMutations.map(mutation => (
                      <div key={mutation.id} className="mutation-card">
                        <div className="mutation-info">
                          <strong>{mutation.type} {mutation.entity}</strong>
                          <span>{JSON.stringify(mutation.payload).substring(0, 50)}...</span>
                        </div>
                        <button 
                          onClick={() => commitMutation(mutation.id)}
                          className="btn-primary"
                        >
                          Approve
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="quick-capture-section">
                <form onSubmit={handleQuickCapture} className="quick-capture-form">
                  <textarea 
                    name="note-content"
                    placeholder="What's on your mind? Capture it here..."
                    className="capture-input"
                  />
                  <div className="form-actions">
                    <span className="hint">Press Cmd+Enter to save</span>
                    <button type="submit" className="btn-primary">Save Note</button>
                  </div>
                </form>
              </div>

              <div className="dashboard-grid">
                <div className="task-section">
                  <div className="section-header">
                    <h3>Tasks</h3>
                    <div className="header-btns">
                      <button 
                        className="btn-ai-action"
                        onClick={() => requestAI('jules', 'task-generator', { projectId: activeProjectId })}
                      >
                        ✨ Ask AI
                      </button>
                      <span className="count">{filteredTasks.length}</span>
                    </div>
                  </div>
                  <div className="task-list-container">
                    {filteredTasks.length > 0 ? (
                      <div className="task-grid">
                        {filteredTasks.map((task) => {
                          const status = task.data?.status || 'todo';
                          return (
                            <div key={task.id} className={`task-card status-${status}`}>
                              <div className="task-body">
                                <h3>{task.title}</h3>
                                <p>{task.description}</p>
                                <div className="task-footer">
                                  <span className={`status-badge badge-${status}`}>
                                    {status.replace('_', ' ')}
                                  </span>
                                  <button 
                                    className="btn-status-toggle"
                                    onClick={() => handleToggleStatus(task)}
                                  >
                                    Toggle
                                  </button>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    ) : (
                      <div className="empty-state">No tasks found.</div>
                    )}
                  </div>
                </div>

                <div className="note-section">
                  <div className="section-header">
                    <h3>Recent Notes</h3>
                    <span className="count">{filteredNotes.length}</span>
                  </div>
                  <div className="note-list">
                    {filteredNotes.length > 0 ? (
                      filteredNotes.map((note) => (
                        <div key={note.id} className="note-card">
                          <div className="note-content">{note.data?.content || note.description}</div>
                          <div className="note-meta">
                            {new Date(note.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="empty-state">No notes yet.</div>
                    )}
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="models-manager">
              <div className="section-header">
                <h2>AI Model Management</h2>
              </div>
              
              <div className="runtime-status-bar">
                {runtimes.length > 0 ? runtimes.map(runtime => (
                  <div key={runtime.id} className={`runtime-badge status-${runtime.status}`}>
                    {runtime.name}: {runtime.status}
                  </div>
                )) : (
                  <div className="runtime-badge status-offline">No runtimes registered.</div>
                )}
              </div>

              <div className="model-grid">
                {models.map(model => (
                  <div key={model.id} className="model-card">
                    <div className="model-header">
                      <h3>{model.name}</h3>
                      <span className="engine-tag">{model.engine}</span>
                    </div>
                    <p>{model.sizeGB}GB • {model.capabilities.join(', ')}</p>
                    
                    {model.status === 'missing' && (
                      <button 
                        onClick={() => downloadModel(model.id)}
                        className="btn-primary"
                      >
                        Download Model
                      </button>
                    )}
                    
                    {model.status === 'downloading' && (
                      <div className="download-progress">
                        <div className="progress-bar">
                          <div 
                            className="progress-fill" 
                            style={{ width: `${downloadProgress[model.id] || 0}%` }}
                          />
                        </div>
                        <span className="progress-text">{downloadProgress[model.id] || 0}%</span>
                      </div>
                    )}
                    
                    {model.status === 'available' && (
                      <div className="available-badge">✅ Ready to use</div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </section>
      </main>

      {undoHistory.length > 0 && (
        <div className="undo-toast">
          <div className="undo-message">Change committed.</div>
          <button onClick={() => undo()} className="btn-undo">
            <span className="undo-icon">⟲</span>
            Undo
          </button>
        </div>
      )}

      <style>{`
        :root {
          --bg-primary: #0f1115;
          --bg-secondary: #16191e;
          --bg-sidebar: #0a0c0f;
          --accent: #4f46e5;
          --text-primary: #e2e8f0;
          --text-secondary: #94a3b8;
          --border: #1e293b;
          --status-todo: #64748b;
          --status-progress: #3b82f6;
          --status-done: #10b981;
        }

        * { box-sizing: border-box; }
        body {
          margin: 0;
          font-family: 'Inter', -apple-system, sans-serif;
          background: var(--bg-primary);
          color: var(--text-primary);
        }

        .app-container {
          display: flex;
          height: 100vh;
          overflow: hidden;
        }

        .sidebar {
          width: 260px;
          background: var(--bg-sidebar);
          border-right: 1px solid var(--border);
          display: flex;
          flex-direction: column;
        }

        .sidebar-header {
          padding: 24px;
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .sidebar-header h1 {
          font-size: 1.2rem;
          margin: 0;
          font-weight: 700;
          letter-spacing: -0.025em;
        }

        .status-dot {
          width: 8px;
          height: 8px;
          background: #10b981;
          border-radius: 50%;
          box-shadow: 0 0 8px rgba(16, 185, 129, 0.5);
        }

        .title-row { display: flex; align-items: center; justify-content: space-between; width: 100%; }
        .status-row { display: flex; align-items: center; justify-content: space-between; width: 100%; margin-top: 8px; }

        .platform-badge {
          font-size: 0.6rem;
          font-weight: 800;
          text-transform: uppercase;
          padding: 2px 6px;
          border-radius: 4px;
          background: var(--bg-secondary);
          border: 1px solid var(--border);
        }
        .platform-android { color: #3ddc84; border-color: #3ddc84; }
        .platform-web { color: #3b82f6; border-color: #3b82f6; }

        .btn-sync-mini {
          background: transparent;
          border: 1px solid var(--accent);
          color: var(--accent);
          font-size: 0.65rem;
          font-weight: 600;
          padding: 2px 8px;
          border-radius: 4px;
          cursor: pointer;
        }
        .btn-sync-mini:hover { background: var(--accent); color: white; }

        .main-nav {
          padding: 0 16px 16px;
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .nav-item {
          width: 100%;
          padding: 8px 12px;
          background: transparent;
          border: none;
          color: var(--text-secondary);
          text-align: left;
          font-size: 0.9rem;
          font-weight: 500;
          cursor: pointer;
          border-radius: 6px;
          transition: all 0.2s;
        }

        .nav-item:hover { background: rgba(255, 255, 255, 0.05); }
        .nav-item.active { background: var(--bg-secondary); color: var(--text-primary); }

        .section-label {
          padding: 24px 24px 8px;
          font-size: 0.75rem;
          font-weight: 600;
          color: var(--text-secondary);
          text-transform: uppercase;
          letter-spacing: 0.05em;
        }

        .project-list {
          padding: 4px 0;
        }

        .project-item {
          width: 100%;
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 10px 24px;
          background: transparent;
          border: none;
          color: var(--text-secondary);
          cursor: pointer;
          transition: all 0.2s ease;
          text-align: left;
        }

        .project-item:hover {
          background: rgba(255, 255, 255, 0.05);
          color: var(--text-primary);
        }

        .project-item.active {
          background: rgba(79, 70, 229, 0.1);
          color: var(--accent);
          border-right: 2px solid var(--accent);
        }

        .project-icon {
          font-size: 1rem;
          opacity: 0.7;
        }

        .suggested-actions-sidebar {
          margin-top: auto;
          padding-bottom: 24px;
          border-top: 1px solid var(--border);
        }

        .actions-mini-list {
          padding: 0 16px;
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .action-mini-card {
          background: var(--bg-secondary);
          border: 1px solid var(--border);
          border-radius: 8px;
          padding: 12px;
        }

        .action-mini-title {
          font-size: 0.85rem;
          font-weight: 500;
          margin-bottom: 8px;
          color: var(--text-primary);
        }

        .action-mini-btns {
          display: flex;
          gap: 8px;
        }

        .btn-mini {
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid var(--border);
          color: var(--text-secondary);
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 0.75rem;
          cursor: pointer;
          flex: 1;
        }

        .btn-mini:hover { background: rgba(255, 255, 255, 0.1); }
        .btn-accept:hover { background: var(--status-done); color: white; border-color: var(--status-done); }

        .main-content {
          flex: 1;
          display: flex;
          flex-direction: column;
        }

        .content-header {
          height: 64px;
          padding: 0 32px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-bottom: 1px solid var(--border);
          background: var(--bg-secondary);
        }

        .breadcrumb {
          font-size: 0.9rem;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .folder { color: var(--text-secondary); }
        .separator { color: var(--border); }
        .current { font-weight: 600; }

        .header-actions {
          display: flex;
          gap: 12px;
        }

        .btn-icon {
          background: transparent;
          border: none;
          color: var(--text-secondary);
          font-size: 1.2rem;
          cursor: pointer;
          padding: 8px;
          border-radius: 6px;
          transition: background 0.2s;
        }

        .btn-icon:hover {
          background: rgba(255, 255, 255, 0.05);
        }

        .dashboard {
          padding: 40px;
          flex: 1;
          overflow-y: auto;
          display: flex;
          flex-direction: column;
          gap: 32px;
        }

        .project-detail h2, .global-header h2 {
          font-size: 1.8rem;
          margin: 0 0 12px;
        }

        .description {
          font-size: 1rem;
          color: var(--text-secondary);
          line-height: 1.6;
          max-width: 600px;
          margin: 0;
        }

        .metadata {
          margin-top: 24px;
          font-family: monospace;
          font-size: 0.8rem;
          color: var(--accent);
        }

        .pending-mutations-section {
          background: rgba(79, 70, 229, 0.05);
          border: 1px solid var(--accent);
          border-radius: 12px;
          padding: 20px;
        }

        .mutation-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .mutation-card {
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: var(--bg-secondary);
          padding: 12px 16px;
          border-radius: 8px;
          border: 1px solid var(--border);
        }

        .mutation-info {
          display: flex;
          flex-direction: column;
          gap: 4px;
        }

        .mutation-info strong { font-size: 0.9rem; color: var(--accent); }
        .mutation-info span { font-size: 0.8rem; color: var(--text-secondary); font-family: monospace; }

        .quick-capture-section {
          background: var(--bg-secondary);
          border: 1px solid var(--border);
          border-radius: 12px;
          padding: 20px;
        }

        .capture-input {
          width: 100%;
          background: transparent;
          border: none;
          color: var(--text-primary);
          font-size: 1.1rem;
          resize: none;
          min-height: 60px;
          margin-bottom: 12px;
          outline: none;
        }

        .form-actions {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .hint {
          font-size: 0.8rem;
          color: var(--text-secondary);
        }

        .btn-primary {
          background: var(--accent);
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          font-weight: 600;
          cursor: pointer;
          transition: opacity 0.2s;
        }

        .btn-primary:hover { opacity: 0.9; }

        .dashboard-grid {
          display: grid;
          grid-template-columns: 2fr 1fr;
          gap: 32px;
        }

        .section-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
          margin-bottom: 20px;
        }

        .section-header h3 {
          margin: 0;
          font-size: 1rem;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          color: var(--text-secondary);
        }

        .count {
          background: var(--bg-secondary);
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 0.75rem;
          color: var(--text-secondary);
        }

        .btn-ai-action {
          background: rgba(79, 70, 229, 0.1);
          color: var(--accent);
          border: 1px solid var(--accent);
          padding: 4px 12px;
          border-radius: 6px;
          font-size: 0.8rem;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
        }

        .btn-ai-action:hover { background: var(--accent); color: white; }

        .task-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 16px;
        }

        .task-card {
          background: var(--bg-secondary);
          border: 1px solid var(--border);
          border-radius: 10px;
          transition: border-color 0.2s;
        }

        .task-card:hover { border-color: var(--accent); }

        .task-body {
          padding: 20px;
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .task-body h3 {
          margin: 0;
          font-size: 1rem;
        }

        .task-body p {
          margin: 0;
          font-size: 0.85rem;
          color: var(--text-secondary);
          line-height: 1.4;
        }

        .task-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 12px;
        }

        .status-badge {
          font-size: 0.65rem;
          font-weight: 700;
          padding: 2px 6px;
          border-radius: 4px;
        }

        .badge-todo { background: rgba(100, 116, 139, 0.1); color: var(--status-todo); }
        .badge-in_progress { background: rgba(59, 130, 246, 0.1); color: var(--status-progress); }
        .badge-done { background: rgba(16, 185, 129, 0.1); color: var(--status-done); }

        .btn-status-toggle {
          background: transparent;
          border: 1px solid var(--border);
          color: var(--text-secondary);
          font-size: 0.75rem;
          padding: 2px 8px;
          border-radius: 4px;
          cursor: pointer;
        }

        .note-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .note-card {
          background: var(--bg-secondary);
          border: 1px solid var(--border);
          border-radius: 10px;
          padding: 16px;
        }

        .note-content {
          font-size: 0.9rem;
          line-height: 1.5;
          margin-bottom: 8px;
          white-space: pre-wrap;
        }

        .note-meta {
          font-size: 0.75rem;
          color: var(--text-secondary);
        }

        .empty-state {
          padding: 40px;
          text-align: center;
          color: var(--text-secondary);
          border: 1px dashed var(--border);
          border-radius: 12px;
          font-size: 0.9rem;
        }

        .undo-toast {
          position: fixed;
          bottom: 24px;
          left: 50%;
          transform: translateX(-50%);
          background: #1e293b;
          border: 1px solid var(--accent);
          padding: 12px 24px;
          border-radius: 100px;
          display: flex;
          align-items: center;
          gap: 16px;
          box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
          animation: slideUp 0.3s ease-out;
          z-index: 1000;
        }

        .undo-message { font-size: 0.9rem; font-weight: 500; }

        .btn-undo {
          background: var(--accent);
          color: white;
          border: none;
          padding: 6px 16px;
          border-radius: 50px;
          font-size: 0.85rem;
          font-weight: 600;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 6px;
        }

        .undo-icon { font-size: 1.1rem; }

        .models-manager {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .runtime-status-bar {
          display: flex;
          gap: 12px;
        }

        .runtime-badge {
          font-size: 0.8rem;
          padding: 4px 12px;
          border-radius: 100px;
          background: var(--bg-secondary);
          border: 1px solid var(--border);
        }

        .runtime-badge.status-online { border-color: var(--status-done); color: var(--status-done); }
        .runtime-badge.status-offline { border-color: #ef4444; color: #ef4444; }

        .model-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
          gap: 20px;
        }

        .model-card {
          background: var(--bg-secondary);
          border: 1px solid var(--border);
          border-radius: 12px;
          padding: 24px;
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .model-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .engine-tag {
          font-size: 0.7rem;
          padding: 2px 8px;
          background: var(--accent);
          border-radius: 4px;
          font-weight: 700;
        }

        .available-badge { font-size: 0.9rem; color: var(--status-done); font-weight: 600; }

        .download-progress {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .progress-bar {
          height: 8px;
          background: var(--bg-primary);
          border-radius: 4px;
          overflow: hidden;
        }

        .progress-fill { height: 100%; background: var(--accent); transition: width 0.3s; }
        .progress-text { font-size: 0.8rem; color: var(--text-secondary); text-align: right; }

        @keyframes slideUp {
          from { transform: translate(-50%, 100%); opacity: 0; }
          to { transform: translate(-50%, 0); opacity: 1; }
        }
      `}</style>
    </div>
  );
}

export default App;
