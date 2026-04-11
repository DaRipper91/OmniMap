import { Capacitor } from '@capacitor/core';
import { Filesystem, Directory, Encoding } from '@capacitor/filesystem';
import { Http } from '@capacitor/http';
import {} from './types';
import {} from './native-bridge';
/**
 * Core implementation of the Singularity Native Bridge using @capacitor/http.
 */
export const getPlatform = () => {
    const capPlatform = Capacitor.getPlatform();
    if (capPlatform === 'android' || capPlatform === 'ios' || capPlatform === 'web') {
        return capPlatform;
    }
    return 'web';
};
export const nativeFS = {
    writeNode: async (node, projectPath) => {
        const platform = getPlatform();
        const fileName = `${node.id}.md`;
        const content = `---\ntitle: ${node.title}\ntype: ${node.type}\n---\n\n${node.description}`;
        if (platform === 'web') {
            localStorage.setItem(`node_${node.id}`, JSON.stringify(node));
            return;
        }
        await Filesystem.writeFile({
            path: `${projectPath}/${fileName}`,
            data: content,
            directory: Directory.Documents,
            encoding: Encoding.UTF8,
            recursive: true
        });
    },
    readNode: async (filePath) => {
        const platform = getPlatform();
        if (platform === 'web')
            throw new Error('Not supported on web');
        const result = await Filesystem.readFile({
            path: filePath,
            directory: Directory.Documents,
            encoding: Encoding.UTF8
        });
        return {
            id: filePath.split('/').pop()?.replace('.md', '') || 'unknown',
            type: 'IDEA',
            title: 'Parsed Node',
            description: result.data,
            data: {},
            metadata: {},
            createdAt: Date.now(),
            updatedAt: Date.now()
        };
    },
    scanDir: async (path) => {
        const platform = getPlatform();
        if (platform === 'web')
            return [];
        const result = await Filesystem.readdir({
            path,
            directory: Directory.Documents
        });
        return result.files.map(f => f.name);
    },
    requestPermissions: async () => {
        const platform = getPlatform();
        if (platform === 'web')
            return true;
        const status = await Filesystem.requestPermissions();
        return status.publicStorage === 'granted';
    }
};
export const nativeHTTP = {
    downloadFile: async (url, targetPath, onProgress) => {
        const platform = getPlatform();
        if (platform === 'web') {
            window.open(url, '_blank');
            return;
        }
        onProgress(10);
        await Http.downloadFile({
            url,
            filePath: targetPath,
            fileDirectory: Directory.Documents
        });
        onProgress(100);
    }
};
//# sourceMappingURL=native-bridge-impl.js.map