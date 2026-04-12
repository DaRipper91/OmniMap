document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const data = JSON.parse(e.target.result);
            renderGraph(data);
        } catch (err) {
            alert('Invalid OmniMap JSON file.');
            console.error(err);
        }
    };
    reader.readAsText(file);
});

function renderGraph(data) {
    const nodeLayer = document.getElementById('node-layer');
    const edgeLayer = document.getElementById('edge-layer');
    
    // Clear previous
    nodeLayer.innerHTML = '';
    edgeLayer.innerHTML = '';

    if (!data.nodes) return;

    // Create a lookup map for edges
    const nodesMap = {};

    // Render Nodes
    data.nodes.forEach(node => {
        nodesMap[node.id] = node;

        const nodeEl = document.createElement('div');
        nodeEl.className = 'node';
        // Apply position
        nodeEl.style.left = `${node.x}px`;
        nodeEl.style.top = `${node.y}px`;

        const titleEl = document.createElement('div');
        titleEl.className = 'title';
        titleEl.textContent = node.title;

        const descEl = document.createElement('div');
        descEl.className = 'description';
        descEl.textContent = node.description || '';

        nodeEl.appendChild(titleEl);
        nodeEl.appendChild(descEl);
        nodeLayer.appendChild(nodeEl);
    });

    // Render Edges
    if (data.edges) {
        data.edges.forEach(edge => {
            const source = nodesMap[edge.sourceId];
            const target = nodesMap[edge.targetId];

            if (source && target) {
                // Approximate center of a 200px wide, ~100px tall node
                const startX = source.x + 100;
                const startY = source.y + 50;
                const endX = target.x + 100;
                const endY = target.y + 50;

                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', startX);
                line.setAttribute('y1', startY);
                line.setAttribute('x2', endX);
                line.setAttribute('y2', endY);
                line.setAttribute('stroke', '#49454F'); // MD3 Outline
                line.setAttribute('stroke-width', '4');
                
                edgeLayer.appendChild(line);
            }
        });
    }
}