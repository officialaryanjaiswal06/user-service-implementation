import { Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';

export function RequireRole({ anyOf, children }: { anyOf: string[]; children: React.ReactNode }) {
  const { user, isAuthenticated } = useAuth();
  const roles = user?.roles || [];
  const ok = anyOf.some((r) => roles.includes(r));

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!ok) return <Navigate to="/" replace />;

  return <>{children}</>;
}
