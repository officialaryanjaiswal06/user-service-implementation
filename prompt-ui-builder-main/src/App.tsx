import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { ThemeProvider } from "next-themes";
import { AuthProvider } from "@/contexts/AuthContext";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { RequireRole } from "@/components/RequireRole";
import { Navigation } from "@/components/Navigation";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import UserList from "./pages/admin/users/List";
import UserCreate from "./pages/admin/users/Create";
import UserEdit from "./pages/admin/users/Edit";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <ThemeProvider attribute="class" defaultTheme="light" enableSystem>
      <AuthProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <BrowserRouter>
            <Navigation />
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <Dashboard />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <Profile />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/admin/users"
                element={
                  <RequireRole anyOf={["ROLE_ADMIN", "ROLE_SUPER_ADMIN"]}>
                    <UserList />
                  </RequireRole>
                }
              />
              <Route
                path="/admin/users/new"
                element={
                  <RequireRole anyOf={["ROLE_ADMIN", "ROLE_EDITOR", "ROLE_SUPER_ADMIN"]}>
                    <UserCreate />
                  </RequireRole>
                }
              />
              <Route
                path="/admin/users/:id/edit"
                element={
                  <RequireRole anyOf={["ROLE_ADMIN", "ROLE_EDITOR", "ROLE_SUPER_ADMIN"]}>
                    <UserEdit />
                  </RequireRole>
                }
              />

              {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </AuthProvider>
    </ThemeProvider>
  </QueryClientProvider>
);

export default App;
