import type { Meta, StoryObj } from '@storybook/react';
import { RegistrationTitle } from '../components/register/RegistrationTitle';
import { withMantineContext } from '../decorators/withMantineContext';

const meta: Meta<typeof RegistrationTitle> = {
  title: 'RegistrationTitle',
  component: RegistrationTitle,
  // Add a decorator to wrap the component with MantineProvider
  decorators: [withMantineContext],
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof RegistrationTitle>;

export const Default: Story = {
  args: {},
};