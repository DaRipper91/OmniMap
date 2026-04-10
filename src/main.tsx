import React, { useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { useContinuumStore } from './store';

const Root = () => {
  const addProject = useContinuumStore((state) => state.addProject);
  const projects = useContinuumStore((state) => state.projects);
  const addTask = useContinuumStore((state) => state.addTask);
  const tasks = useContinuumStore((state) => state.tasks);
  const addSuggestedAction = useContinuumStore((state) => state.addSuggestedAction);
  const suggestedActions = useContinuumStore((state) => state.suggestedActions);
  const migrateToGraph = useContinuumStore((state) => state.migrateToGraph);
  const nodes = useContinuumStore((state) => state.nodes);

  // Initialize with a mock project if empty for testing
  useEffect(() => {
    if (projects.length === 0) {
      addProject({
        id: 'continuum-core',
        name: 'Continuum Core',
        description: 'Development of the Continuum system',
        path: '/data/data/com.termux/files/home/repo/contiinuum',
        createdAt: Date.now(),
        updatedAt: Date.now(),
        tags: ['internal', 'active'],
      });
      
      addProject({
        id: 'external-agent-x',
        name: 'Agent X Research',
        description: 'Testing external agent integrations',
        path: '/data/data/com.termux/files/home/repo/agent-x',
        createdAt: Date.now(),
        updatedAt: Date.now(),
        tags: ['research'],
      });
    }

    if (tasks.length === 0) {
      addTask({
        id: 'task-1',
        projectId: 'continuum-core',
        title: 'Implement Task List View',
        description: 'Create the React component for displaying project tasks.',
        status: 'todo' as any,
        priority: 1,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        tags: ['ui', 'phase-6'],
      });
      
      addTask({
        id: 'task-2',
        projectId: 'continuum-core',
        title: 'Define Style Tokens',
        description: 'Set up CSS variables for consistent Pixel-pro aesthetics.',
        status: 'in_progress' as any,
        priority: 2,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        tags: ['style'],
      });

      addTask({
        id: 'task-3',
        projectId: 'global',
        title: 'Buy Groceries',
        description: 'External non-project task.',
        status: 'todo' as any,
        priority: 3,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        tags: ['personal'],
      });
    }

    if (suggestedActions.length === 0) {
      addSuggestedAction({
        id: 'action-1',
        projectId: 'continuum-core',
        title: 'Update UI Rules',
        description: 'AI detected outdated UI rules in docs. Suggesting update.',
        status: 'active' as any,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        actionType: 'refactor',
        metadata: { path: 'docs/rules.md', content: 'Updated content...' }
      });
      
      addSuggestedAction({
        id: 'action-2',
        projectId: 'external-agent-x',
        title: 'Create Research Note',
        description: 'Summarize the latest findings on agent integration.',
        status: 'active' as any,
        createdAt: Date.now(),
        updatedAt: Date.now(),
        actionType: 'create_note',
        metadata: { content: 'Summary of findings...' }
      });
    }
  }, [projects.length, tasks.length, suggestedActions.length, addProject, addTask, addSuggestedAction]);

  // Migration Effect
  useEffect(() => {
    if (nodes.length === 0 && projects.length > 0) {
      console.log('Migrating legacy data to Node Graph...');
      migrateToGraph();
    }
  }, [nodes.length, projects.length, migrateToGraph]);

  return (
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(<Root />);
