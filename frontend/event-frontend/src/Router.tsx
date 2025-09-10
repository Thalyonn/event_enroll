import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { HomePage } from './pages/Home.page';
import { MainLayout } from './layout/MainLayout';
import { BlankLayout } from './layout/BlankLayout';
import { RegisterPage } from './pages/Register.page';
import { LoginPage } from './pages/Login.page';

const router = createBrowserRouter([
  {
    element: <MainLayout/>,
    children: [
      {
        path: '/',
    element: <HomePage />,
      },
    ],
    
  },
  {
    element: <BlankLayout />,
    children: [
      {
        path: '/login',
        element: <LoginPage />
      },
      {
        path: '/register',
        element: <RegisterPage />
      },
    ],
  },
]);

export function Router() {
  return <RouterProvider router={router} />;
}
