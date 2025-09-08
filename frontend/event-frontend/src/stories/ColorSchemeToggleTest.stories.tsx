import type { Meta, StoryObj } from '@storybook/react';
import { ColorSchemeToggle } from '../components/ColorSchemeToggle/ColorSchemeToggle';
import { withMantineContext } from '../decorators/withMantineContext';

const meta: Meta<typeof ColorSchemeToggle> = {
  title: 'ColorSchemeToggle',
  component: ColorSchemeToggle,
  // Add a decorator to wrap the component with MantineProvider
  decorators: [withMantineContext],
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof ColorSchemeToggle>;

export const Default: Story = {
  args: {},
};