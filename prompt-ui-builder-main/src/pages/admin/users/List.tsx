import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listUsers, deleteUser, User } from '@/lib/usersApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useNavigate } from 'react-router-dom';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';

export default function UserList() {
  const qc = useQueryClient();
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user } = useAuth();

  const { data, isLoading } = useQuery<User[]>({ queryKey: ['users'], queryFn: listUsers });

  const del = useMutation({
    mutationFn: (id: number) => deleteUser(id),
    onSuccess: () => {
      toast({ title: 'User deleted' });
      qc.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (e: any) => {
      toast({ variant: 'destructive', title: 'Delete failed', description: e?.response?.data?.message || e.message });
    },
  });

  return (
    <div className="container py-6">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Users</h1>
        <div className="flex gap-2">
          {(user?.roles || []).some(r => r === 'ROLE_ADMIN' || r === 'ROLE_EDITOR' || r === 'ROLE_SUPER_ADMIN') && (
            <Button onClick={() => navigate('/admin/users/new')}>Create User</Button>
          )}
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>User List</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p>Loading...</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Username</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Roles</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell>{u.id}</TableCell>
                    <TableCell>{u.username}</TableCell>
                    <TableCell>{u.email || '-'}</TableCell>
                    <TableCell>{(u.roles || []).map(r => r.replace('ROLE_', '')).join(', ')}</TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button variant="outline" onClick={() => navigate(`/admin/users/${u.id}/edit`)}>Edit</Button>
                        <Button variant="destructive" onClick={() => del.mutate(u.id)} disabled={del.isPending}>Delete</Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
