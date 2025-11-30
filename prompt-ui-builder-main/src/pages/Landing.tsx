import { Link } from 'react-router-dom';
import { Shield, Lock, Zap, CheckCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export default function Landing() {
  const features = [
    {
      icon: Shield,
      title: 'Enterprise Security',
      description: 'JWT-based authentication with industry-standard encryption',
    },
    {
      icon: Lock,
      title: 'Role-Based Access',
      description: 'Granular permission control for different user types',
    },
    {
      icon: Zap,
      title: 'Lightning Fast',
      description: 'Optimized performance with instant token validation',
    },
  ];

  return (
    <div className="flex min-h-screen flex-col">
      {/* Hero Section */}
      <section className="bg-gradient-to-b from-background to-muted py-20 md:py-32">
        <div className="container">
          <div className="mx-auto max-w-3xl text-center">
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border bg-background px-4 py-2 text-sm">
              <Shield className="h-4 w-4 text-primary" />
              <span>Secure by Design</span>
            </div>
            
            <h1 className="mb-6 text-4xl font-bold tracking-tight md:text-6xl">
              Modern Authentication
              <span className="block text-primary">Made Simple</span>
            </h1>
            
            <p className="mb-8 text-lg text-muted-foreground md:text-xl">
              Enterprise-grade user authentication with JWT tokens. Secure, scalable,
              and ready to integrate with your applications.
            </p>
            
            <div className="flex flex-col gap-4 sm:flex-row sm:justify-center">
              <Button asChild size="lg" className="text-base">
                <Link to="/register">Get Started Free</Link>
              </Button>
              <Button asChild size="lg" variant="outline" className="text-base">
                <Link to="/login">Sign In</Link>
              </Button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20">
        <div className="container">
          <div className="mb-12 text-center">
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Built for Developers
            </h2>
            <p className="text-lg text-muted-foreground">
              Everything you need for secure user authentication
            </p>
          </div>

          <div className="grid gap-8 md:grid-cols-3">
            {features.map((feature) => (
              <Card key={feature.title} className="border-2 transition-shadow hover:shadow-lg">
                <CardContent className="pt-6">
                  <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
                    <feature.icon className="h-6 w-6 text-primary" />
                  </div>
                  <h3 className="mb-2 text-xl font-semibold">{feature.title}</h3>
                  <p className="text-muted-foreground">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="bg-muted py-20">
        <div className="container">
          <div className="mx-auto max-w-3xl">
            <h2 className="mb-12 text-center text-3xl font-bold md:text-4xl">
              Why Choose SecureAuth?
            </h2>
            
            <div className="space-y-4">
              {[
                'Industry-standard JWT token authentication',
                'Secure password hashing with bcrypt',
                'RESTful API design for easy integration',
                'PostgreSQL-backed data persistence',
                'Role-based authorization system',
              ].map((benefit) => (
                <div key={benefit} className="flex items-start gap-3">
                  <CheckCircle className="mt-1 h-5 w-5 flex-shrink-0 text-primary" />
                  <p className="text-lg">{benefit}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20">
        <div className="container">
          <Card className="border-2 border-primary/20 bg-gradient-to-br from-primary/5 to-primary/10">
            <CardContent className="p-12 text-center">
              <h2 className="mb-4 text-3xl font-bold md:text-4xl">
                Ready to Get Started?
              </h2>
              <p className="mb-8 text-lg text-muted-foreground">
                Create your account in seconds and experience secure authentication
              </p>
              <Button asChild size="lg" className="text-base">
                <Link to="/register">Create Account</Link>
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  );
}
