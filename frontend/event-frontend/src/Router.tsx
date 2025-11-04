import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { HomePage } from './pages/Home.page';
import { MainLayout } from './layout/MainLayout';
import { BlankLayout } from './layout/BlankLayout';
import { RegisterPage } from './pages/Register.page';
import { LoginPage } from './pages/Login.page';
import { CreateEventPage } from './pages/Create.page'
import { ProtectedRoute } from './components/ProtectedRoute';
import { EventPage } from './pages/Event.page';
import { ErrorNotFound } from './pages/ErrorNotFound.page';
import { EditEventPage } from './pages/Edit.page';

const router = createBrowserRouter([
  {
    element: <MainLayout/>,
    children: [
      {
        path: '/',
        element: <HomePage />,
      },
      {
        path: '/event/:id',
        element: <EventPage />
      },
      {
        path: '*', //404 on unknown routes
        element: <ErrorNotFound />,
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
      {
        path: '/create',
        element: (
          <ProtectedRoute requireAdmin>
            <CreateEventPage />
          </ProtectedRoute>
          
        ),
      },
      {
        path: '/edit/:id',
        element: (
          <ProtectedRoute requireAdmin>
            <EditEventPage />
          </ProtectedRoute>
        )
      },
    ],
  },
]);

export function Router() {
  return <RouterProvider router={router} />;
}
