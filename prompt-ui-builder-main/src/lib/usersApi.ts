import { api } from './api';

export type User = {
  id: number;
  username: string;
  email?: string;
  roles: string[];
};

export type CreateUserPayload = {
  username: string;
  email: string;
  password: string;
  roles?: string[]; // names like 'ROLE_USER'
};

export type UpdateUserPayload = {
  email?: string;
  password?: string;
};

export type UpdateRolesPayload = {
  roles: string[];
};

export async function me(): Promise<User> {
  const { data } = await api.get('/users/me');
  return data;
}

export async function listUsers(): Promise<User[]> {
  const { data } = await api.get('/users');
  return data;
}

export async function getUser(id: number): Promise<User> {
  const { data } = await api.get(`/users/${id}`);
  return data;
}

export async function createUser(payload: CreateUserPayload): Promise<User> {
  // backend expects roles as set of objects or names? Our controller accepts CreateUserRequest with Set<String> roles (names).
  const body: any = {
    username: payload.username,
    email: payload.email,
    password: payload.password,
    roles: payload.roles ?? ['ROLE_USER'],
  };
  const { data } = await api.post('/users', body);
  return data;
}

export async function updateUser(id: number, payload: UpdateUserPayload): Promise<User> {
  const { data } = await api.put(`/users/${id}`, payload);
  return data;
}

export async function updateUserRoles(id: number, payload: UpdateRolesPayload): Promise<User> {
  const { data } = await api.put(`/users/${id}/roles`, payload);
  return data;
}

export async function deleteUser(id: number): Promise<void> {
  await api.delete(`/users/${id}`);
}

// ----- Permission management (SUPER_ADMIN / ADMIN via IAM endpoints) -----

export async function getUserPermissions(id: number): Promise<string[]> {
  const { data } = await api.get(`/admin/users/${id}/permissions`);
  return data;
}

export async function setUserPermissions(id: number, permissions: string[]): Promise<void> {
  await api.put(`/admin/users/${id}/permissions`, permissions);
}
