import type { Meta, StoryObj } from '@storybook/react';
import { TableSort } from '@/components/Modals/ViewEnrolled/TableSort'; 
import { withMantineContext } from '../decorators/withMantineContext';

const meta: Meta<typeof TableSort> = {
  title: 'TableSort',
  component: TableSort,
  decorators: [withMantineContext],
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof TableSort>;

export const Default: Story = {
  args: {},
};