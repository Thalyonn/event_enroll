import type { Meta, StoryObj } from '@storybook/react';
import { AuthenticationTitle } from '../components/login/AuthenticationTitle';
import { withMantineContext } from '../decorators/withMantineContext';

const meta: Meta<typeof AuthenticationTitle> = {
  title: 'AuthenticationTitle',
  component: AuthenticationTitle,
  // Add a decorator to wrap the component with MantineProvider
  decorators: [withMantineContext],
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof AuthenticationTitle>;

export const Default: Story = {
  args: {},
};