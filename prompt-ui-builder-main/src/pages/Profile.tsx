import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { me, updateUser, User } from '@/lib/usersApi';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useState } from 'react';
import { useToast } from '@/hooks/use-toast';

export default function Profile() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery<User>({ queryKey: ['me'], queryFn: me });
  const { toast } = useToast();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const save = useMutation({
    mutationFn: async () => {
      if (!data) return;
      return updateUser(data.id, { email: email || undefined, password: password || undefined });
    },
    onSuccess: () => {
      toast({ title: 'Profile updated' });
      qc.invalidateQueries({ queryKey: ['me'] });
      setPassword('');
    },
    onError: (e: any) => toast({ variant: 'destructive', title: 'Update failed', description: e?.response?.data?.message || e.message })
  });

  if (isLoading || !data) return <div className="container py-6">Loading...</div>;

  return (
    <div className="container py-6">
      <Card>
        <CardHeader>
          <CardTitle>My Profile</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Username</Label>
            <Input value={data.username} disabled />
          </div>
          <div className="space-y-2">
            <Label>Email</Label>
            <Input defaultValue={data.email || ''} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label>New Password (optional)</Label>
            <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button onClick={() => save.mutate()} disabled={save.isPending}>Save</Button>
        </CardContent>
      </Card>
    </div>
  );
}
