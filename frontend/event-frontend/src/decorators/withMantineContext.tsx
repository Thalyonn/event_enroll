import { MantineProvider, ColorSchemeScript } from '@mantine/core';

export const withMantineContext = (Story: any) => {
  return (
    <>
      <ColorSchemeScript />
      <MantineProvider>
        <Story />
      </MantineProvider>
    </>
  );
};
