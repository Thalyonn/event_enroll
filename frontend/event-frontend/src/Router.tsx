import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { HomePage } from './pages/Home.page';
import { MainLayout } from './layout/MainLayout';

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
]);

export function Router() {
  return <RouterProvider router={router} />;
}
