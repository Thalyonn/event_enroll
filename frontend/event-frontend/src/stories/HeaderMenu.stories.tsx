import type { Meta, StoryObj } from '@storybook/react';
import { HeaderMenu } from '../components/HeaderMenu/HeaderMenu';
import { withMantineContext } from '../decorators/withMantineContext';
import { MemoryRouter } from 'react-router-dom';

const meta: Meta<typeof HeaderMenu> = {
  title: 'HeaderMenu',
  component: HeaderMenu,
  decorators: [
    (Story) => (
      <MemoryRouter>
        <Story />
      </MemoryRouter>
    ),
    withMantineContext,
  ],
  parameters: {
    layout: 'fullscreen',
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof HeaderMenu>;

function mockFetch(response: any, ok = true) {
  global.fetch = async () =>
    new Response(JSON.stringify(response), { status: ok ? 200 : 401 });
}

export const NotAuthenticated: Story = {
  render: () => {
    mockFetch({}, false); // simulate 401
    return <HeaderMenu />;
  },
};

export const AuthenticatedUser: Story = {
  render: () => {
    mockFetch({
      roles: [{ authority: 'ROLE_USER' }],
    });
    return <HeaderMenu />;
  },
};

export const AuthenticatedAdmin: Story = {
  render: () => {
    mockFetch({
      roles: [{ authority: 'ROLE_ADMIN' }],
    });
    return <HeaderMenu />;
  },
};
