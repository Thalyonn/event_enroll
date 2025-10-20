import MDEditor from '@uiw/react-md-editor';
import rehypeSanitize from 'rehype-sanitize';

import { useMantineColorScheme } from '@mantine/core';
import styles from './MarkdownContent.module.css';

export function MarkdownComponent({ markdown } : { markdown?: string }) {
    const {colorScheme} = useMantineColorScheme();
    const mode = colorScheme === 'dark' ? 'dark' : 'light';

    return (
        <div className={styles.container} data-color-mode={mode}>
            <MDEditor.Markdown 
                source={markdown || ''}
                rehypePlugins={[[rehypeSanitize]]}
                className={styles.markdown}
                
            />
        </div>
    )

}