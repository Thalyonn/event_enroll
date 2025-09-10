import { Outlet } from 'react-router-dom';

export function BlankLayout() {
  return (
    <main>
      <Outlet />
    </main>
  );
}