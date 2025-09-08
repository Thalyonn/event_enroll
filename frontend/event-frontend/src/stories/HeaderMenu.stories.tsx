import type { Meta, StoryObj } from '@storybook/react';
import { HeaderMenu } from '../components/HeaderMenu/HeaderMenu';
import { withMantineContext } from '../decorators/withMantineContext';

const meta: Meta<typeof HeaderMenu> = {
  title: 'HeaderMenu',
  component: HeaderMenu,
  // Add the decorator to wrap the component with MantineProvider
  decorators: [withMantineContext],
  parameters: {
    layout: 'fullscreen', // Use fullscreen layout to show the header at the top
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof HeaderMenu>;

export const Default: Story = {
  args: {},
};