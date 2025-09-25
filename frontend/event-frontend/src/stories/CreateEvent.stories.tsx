import type { Meta, StoryObj } from '@storybook/react';
import { withMantineContext } from '../decorators/withMantineContext';
import { CreateEvent } from '@/components/CreateEvent/CreateEvent';

const meta: Meta<typeof CreateEvent> = {
  title: 'CreateEvent',
  component: CreateEvent,
  decorators: [withMantineContext],
  parameters: {
    layout: 'centered', 
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof CreateEvent>;

export const Default: Story = {
  args: {},
};