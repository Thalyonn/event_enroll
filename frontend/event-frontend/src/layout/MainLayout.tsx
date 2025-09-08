import { Outlet } from 'react-router-dom';
import { HeaderMenu } from '../components/HeaderMenu/HeaderMenu';

export function MainLayout() {
  return (
    <>
      <HeaderMenu />
      <main>
        <Outlet />
      </main>
    </>
  );
}