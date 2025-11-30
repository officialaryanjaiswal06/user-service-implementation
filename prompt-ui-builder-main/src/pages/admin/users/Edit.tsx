import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUser, updateUser, updateUserRoles, getUserPermissions, setUserPermissions, User } from '@/lib/usersApi';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { useEffect, useState } from 'react';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';

export default function UserEdit() {
  const { id } = useParams();
  const userId = Number(id);
  const qc = useQueryClient();
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user: me } = useAuth();

  const { data, isLoading } = useQuery<User>({ queryKey: ['user', userId], queryFn: () => getUser(userId) });

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<string>('');
  const [permissions, setPermissions] = useState<string[]>([]);

  const isAdmin = (me?.roles || []).some(r => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN');

  // Load current fine-grained permissions for this user (only if admin/super-admin)
  const permsQuery = useQuery<string[]>({
    queryKey: ['user-permissions', userId],
    queryFn: () => getUserPermissions(userId),
    enabled: isAdmin,
  });

  useEffect(() => {
    if (permsQuery.data) {
      setPermissions(permsQuery.data);
    }
  }, [permsQuery.data]);

  const saveInfo = useMutation({
    mutationFn: () => updateUser(userId, { email: email || undefined, password: password || undefined }),
    onSuccess: (u) => {
      toast({ title: 'User updated' });
      qc.invalidateQueries({ queryKey: ['user', userId] });
      setPassword('');
      if (me?.id === u.id) {
        // Optionally refresh token if backend issues new one on profile update (not implemented)
      }
    },
    onError: (e: any) => toast({ variant: 'destructive', title: 'Update failed', description: e?.response?.data?.message || e.message })
  });

  const saveRole = useMutation({
    mutationFn: () => updateUserRoles(userId, { roles: [role] }),
    onSuccess: () => {
      toast({ title: 'Roles updated' });
      qc.invalidateQueries({ queryKey: ['user', userId] });
    },
    onError: (e: any) => toast({ variant: 'destructive', title: 'Role update failed', description: e?.response?.data?.message || e.message })
  });

  const savePermissionsMutation = useMutation({
    mutationFn: () => setUserPermissions(userId, permissions),
    onSuccess: () => {
      toast({ title: 'Permissions updated' });
      qc.invalidateQueries({ queryKey: ['user-permissions', userId] });
    },
    onError: (e: any) => toast({ variant: 'destructive', title: 'Permissions update failed', description: e?.response?.data?.message || e.message })
  });

  const togglePermission = (code: string) => {
    setPermissions((prev) =>
      prev.includes(code) ? prev.filter((p) => p !== code) : [...prev, code]
    );
  };

  const hasPermission = (code: string) => permissions.includes(code);

  if (isLoading || !data) return <div className="container py-6">Loading...</div>;

  return (
    <div className="container py-6 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Edit User: {data.username}</h1>
        <Button variant="ghost" onClick={() => navigate(-1)}>Back</Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Basic Info</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Email</Label>
            <Input defaultValue={data.email || ''} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label>New Password (optional)</Label>
            <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button onClick={() => saveInfo.mutate()} disabled={saveInfo.isPending}>Save</Button>
        </CardContent>
      </Card>

      {isAdmin && (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Role</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label>Role</Label>
                <Select onValueChange={setRole} defaultValue={data.roles?.[0] || 'ROLE_USER'}>
                  <SelectTrigger><SelectValue placeholder="Select role" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ROLE_USER">USER</SelectItem>
                    <SelectItem value="ROLE_EDITOR">EDITOR</SelectItem>
                    <SelectItem value="ROLE_ADMIN">ADMIN</SelectItem>
                    <SelectItem value="ROLE_SUPER_ADMIN">SUPER_ADMIN</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <Button onClick={() => saveRole.mutate()} disabled={saveRole.isPending}>Save Role</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Program Permissions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {permsQuery.isLoading ? (
                <p>Loading permissions...</p>
              ) : (
                <>
                  <div className="space-y-2">
                    <Label>Academic</Label>
                    <div className="flex flex-col gap-2">
                      <label className="flex items-center gap-2 text-sm">
                        <Checkbox
                          checked={hasPermission('PROGRAM:ACADEMIC:READ')}
                          onCheckedChange={() => togglePermission('PROGRAM:ACADEMIC:READ')}
                        />
                        <span>Academic Read</span>
                      </label>
                      <label className="flex items-center gap-2 text-sm">
                        <Checkbox
                          checked={hasPermission('PROGRAM:ACADEMIC:UPDATE')}
                          onCheckedChange={() => togglePermission('PROGRAM:ACADEMIC:UPDATE')}
                        />
                        <span>Academic Update</span>
                      </label>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Programme</Label>
                    <div className="flex flex-col gap-2">
                      <label className="flex items-center gap-2 text-sm">
                        <Checkbox
                          checked={hasPermission('PROGRAM:PROGRAMME:READ')}
                          onCheckedChange={() => togglePermission('PROGRAM:PROGRAMME:READ')}
                        />
                        <span>Programme Read</span>
                      </label>
                      <label className="flex items-center gap-2 text-sm">
                        <Checkbox
                          checked={hasPermission('PROGRAM:PROGRAMME:UPDATE')}
                          onCheckedChange={() => togglePermission('PROGRAM:PROGRAMME:UPDATE')}
                        />
                        <span>Programme Update</span>
                      </label>
                    </div>
                  </div>

                  <Button
                    onClick={() => savePermissionsMutation.mutate()}
                    disabled={savePermissionsMutation.isPending}
                  >
                    Save Permissions
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
