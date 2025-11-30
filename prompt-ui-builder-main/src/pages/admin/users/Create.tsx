import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { createUser, setUserPermissions } from '@/lib/usersApi';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';

const schema = z.object({
  username: z.string().min(3),
  email: z.string().email(),
  password: z.string().min(6),
  role: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export default function UserCreate() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const { user } = useAuth();

  const roles = user?.roles || [];
  const isSuperAdmin = roles.includes('ROLE_SUPER_ADMIN');
  const isAdmin = roles.includes('ROLE_ADMIN') && !isSuperAdmin;

  // Fine-grained program permissions for the new user (e.g. Academic / Programme editor)
  const [permissions, setPermissions] = useState<string[]>([]);

  const togglePermission = (code: string) => {
    setPermissions((prev) =>
      prev.includes(code) ? prev.filter((p) => p !== code) : [...prev, code]
    );
  };

  const hasPermission = (code: string) => permissions.includes(code);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', email: '', password: '', role: 'ROLE_USER' },
  });

  const mutation = useMutation({
    mutationFn: async (values: FormValues) => {
      let rolesToAssign: string[];

      if (isSuperAdmin) {
        const role = values.role || 'ROLE_USER';
        rolesToAssign = [role];
      } else if (isAdmin) {
        // ADMIN can only create EDITOR accounts
        rolesToAssign = ['ROLE_EDITOR'];
      } else {
        // e.g. EDITOR creating basic users
        rolesToAssign = ['ROLE_USER'];
      }

      const created = await createUser({
        username: values.username,
        email: values.email,
        password: values.password,
        roles: rolesToAssign,
      });

      // If Admin / Super Admin selected any fine-grained permissions, apply them immediately
      if ((isSuperAdmin || isAdmin) && permissions.length > 0) {
        await setUserPermissions(created.id, permissions);
      }

      return created;
    },
    onSuccess: () => {
      toast({ title: 'User created' });
      navigate('/admin/users');
    },
    onError: (e: any) => {
      toast({ variant: 'destructive', title: 'Create failed', description: e?.response?.data?.message || e.message });
    }
  })

  const onSubmit = (v: FormValues) => mutation.mutate(v);

  return (
    <div className="container py-6">
      <Card>
        <CardHeader>
          <CardTitle>Create User</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField control={form.control} name="username" render={({ field }) => (
                <FormItem>
                  <FormLabel>Username</FormLabel>
                  <FormControl>
                    <Input placeholder="username" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />

              <FormField control={form.control} name="email" render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="email@example.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />

              <FormField control={form.control} name="password" render={({ field }) => (
                <FormItem>
                  <FormLabel>Password</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="password" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )} />

              {isSuperAdmin && (
                <FormField control={form.control} name="role" render={({ field }) => (
                  <FormItem>
                    <FormLabel>Role</FormLabel>
                    <Select onValueChange={field.onChange} defaultValue={field.value}>
                      <SelectTrigger><SelectValue placeholder="Select role" /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ROLE_USER">USER</SelectItem>
                        <SelectItem value="ROLE_EDITOR">EDITOR</SelectItem>
                        <SelectItem value="ROLE_ADMIN">ADMIN</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )} />
              )}

              {(isSuperAdmin || isAdmin) && (
                <div className="space-y-4 border-t pt-4">
                  <h3 className="text-sm font-semibold">Program Permissions</h3>

                  <div className="space-y-2">
                    <FormLabel>Academic</FormLabel>
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
                        <span>Academic Editor (update)</span>
                      </label>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <FormLabel>Programme</FormLabel>
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
                        <span>Programme Editor (update)</span>
                      </label>
                    </div>
                  </div>
                </div>
              )}

              <Button type="submit" disabled={mutation.isPending}>Create</Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
