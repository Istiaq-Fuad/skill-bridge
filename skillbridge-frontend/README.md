# SkillBridge Frontend

A modern, responsive frontend application built with Next.js 15 and TypeScript for the SkillBridge job board platform.

## 🚀 Overview

The SkillBridge frontend provides an intuitive user interface for job seekers and employers to interact with the platform. Built with Next.js App Router, it features modern React patterns, responsive design, and seamless API integration.

## 🏗️ Architecture

```
skillbridge-frontend/
├── src/
│   ├── app/                    # App Router Pages
│   │   ├── layout.tsx          # Root layout
│   │   ├── page.tsx            # Home page
│   │   ├── login/page.tsx      # Login page
│   │   ├── register/page.tsx   # Registration page
│   │   ├── dashboard/page.tsx  # User dashboard
│   │   ├── jobs/
│   │   │   ├── page.tsx        # Jobs listing
│   │   │   └── [id]/page.tsx   # Job details
│   │   ├── profile/page.tsx    # User profile
│   │   └── applications/page.tsx # Applications tracking
│   ├── components/             # Reusable Components
│   │   ├── layouts/
│   │   │   └── DashboardLayout.tsx
│   │   └── ui/                 # shadcn/ui components
│   │       ├── button.tsx
│   │       ├── card.tsx
│   │       ├── input.tsx
│   │       ├── dialog.tsx
│   │       └── ...
│   ├── contexts/               # React Context
│   │   └── AuthContext.tsx     # Authentication state
│   ├── hooks/                  # Custom Hooks
│   │   └── use-mobile.ts       # Mobile detection
│   └── lib/                    # Utilities & API
│       ├── api.ts              # API client
│       └── utils.ts            # Utility functions
├── public/                     # Static Assets
│   ├── next.svg
│   ├── vercel.svg
│   └── ...
├── components.json             # shadcn/ui configuration
├── next.config.ts              # Next.js configuration
├── tailwind.config.ts          # Tailwind CSS config
├── tsconfig.json              # TypeScript config
└── package.json               # Dependencies
```

## 🛠️ Technology Stack

- **Framework**: Next.js 15 with App Router
- **Language**: TypeScript 5
- **Styling**: Tailwind CSS 4
- **UI Components**: Radix UI + shadcn/ui
- **Icons**: Lucide React
- **State Management**: React Context API
- **HTTP Client**: Fetch API with custom ApiClient
- **Form Handling**: React Hook Form (planned)
- **Notifications**: Sonner toast library
- **Theme**: next-themes for dark mode support
- **Build Tool**: Turbopack (Next.js built-in)

## 📦 Key Dependencies

```json
{
  "dependencies": {
    "next": "15.5.0",
    "react": "19.1.0",
    "react-dom": "19.1.0",
    "typescript": "^5",
    "@radix-ui/react-avatar": "^1.1.10",
    "@radix-ui/react-dialog": "^1.1.15",
    "@radix-ui/react-dropdown-menu": "^2.1.16",
    "@radix-ui/react-navigation-menu": "^1.2.14",
    "@radix-ui/react-select": "^2.2.6",
    "@radix-ui/react-tabs": "^1.1.13",
    "class-variance-authority": "^0.7.1",
    "clsx": "^2.1.1",
    "lucide-react": "^0.544.0",
    "next-themes": "^0.4.6",
    "sonner": "^2.0.7",
    "tailwind-merge": "^3.3.1"
  }
}
```

## 🎨 UI Components

### Component Library (shadcn/ui)

- **Interactive**: Button, Dialog, Dropdown Menu, Select, Tabs
- **Form**: Input, Label, Checkbox, Switch, Textarea
- **Feedback**: Alert, Skeleton, Sonner (Toast)
- **Layout**: Card, Separator, Sheet, Sidebar
- **Data Display**: Avatar, Badge, Table, Pagination
- **Navigation**: Navigation Menu, Tooltip

### Custom Components

- **DashboardLayout**: Responsive layout with sidebar navigation
- **AuthContext**: Authentication state management
- **ApiClient**: Centralized API communication

## 🔗 API Integration

### API Client (`src/lib/api.ts`)

```typescript
class ApiClient {
  private baseUrl: string;

  // Authentication
  async login(credentials): Promise<ApiResponse<AuthData>>;
  async register(userData): Promise<ApiResponse<AuthData>>;
  async getProfile(): Promise<ApiResponse<User>>;

  // Jobs
  async getJobs(params?): Promise<ApiResponse<Job[]>>;
  async getJob(id): Promise<ApiResponse<Job>>;
  async createJob(jobData): Promise<ApiResponse<Job>>;

  // Applications
  async applyForJob(jobId): Promise<ApiResponse<JobApplication>>;
  async getUserApplications(userId): Promise<ApiResponse<JobApplication[]>>;

  // Profile Management
  async addEducation(userId, education): Promise<ApiResponse<Education>>;
  async addExperience(userId, experience): Promise<ApiResponse<Experience>>;
  async addSkill(userId, skill): Promise<ApiResponse<Skill>>;
  async addPortfolio(userId, portfolio): Promise<ApiResponse<Portfolio>>;
}
```

### Type Definitions

```typescript
interface User {
  id: number;
  username: string;
  email: string;
  role: "JOB_SEEKER" | "EMPLOYER";
  firstName?: string;
  lastName?: string;
  bio?: string;
  phoneNumber?: string;
  address?: string;
  city?: string;
  country?: string;
}

interface Job {
  id: number;
  title: string;
  description: string;
  company: string;
  location: string;
  salary?: string;
  requirements: string[];
  techStack: string[];
  createdAt: string;
  employerId: number;
}

interface JobApplication {
  id: number;
  jobId: number;
  userId: number;
  status: "APPLIED" | "REVIEWED" | "INTERVIEW" | "REJECTED" | "ACCEPTED";
  appliedAt: string;
  coverLetter?: string;
  resumeUrl?: string;
}
```

## 🧭 Routing & Pages

### Public Routes

- `/` - Home/Landing page
- `/login` - User authentication
- `/register` - User registration
- `/jobs` - Public job listings
- `/jobs/[id]` - Individual job details

### Protected Routes (Authenticated Users)

- `/dashboard` - Role-based dashboard
- `/profile` - User profile management
- `/applications` - Job applications tracking

### Route Protection

```typescript
// Implemented in page components
useEffect(() => {
  if (!isLoading && !user) {
    router.push("/login");
  }
}, [user, isLoading, router]);
```

## 🎯 Features by User Role

### Job Seekers

- **Profile Management**: Complete profile with education, experience, skills, portfolio
- **Job Search**: Browse and search job opportunities with filters
- **Job Applications**: Apply for jobs with cover letters and resume uploads
- **Application Tracking**: Monitor application status and history
- **Dashboard**: Personal analytics and quick actions

### Employers

- **Job Management**: Post, edit, and manage job listings
- **Candidate Review**: Browse applications and candidate profiles
- **Company Profile**: Manage company information and branding
- **Analytics**: Hiring metrics and application insights

### Common Features

- **Authentication**: Secure login/logout with JWT tokens
- **Responsive Design**: Optimized for desktop, tablet, and mobile
- **Real-time Updates**: Live notifications and status updates
- **Modern UI**: Clean, accessible interface with smooth animations

## 🎨 Styling & Design System

### Tailwind CSS Configuration

```javascript
module.exports = {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "hsl(var(--primary))",
        secondary: "hsl(var(--secondary))",
        accent: "hsl(var(--accent))",
        muted: "hsl(var(--muted))",
      },
      fontFamily: {
        sans: ["var(--font-geist-sans)", "sans-serif"],
        mono: ["var(--font-geist-mono)", "monospace"],
      },
    },
  },
};
```

### Design Tokens

- **Colors**: Semantic color system with CSS variables
- **Typography**: Geist Sans and Geist Mono font families
- **Spacing**: Consistent spacing scale using Tailwind
- **Animations**: Smooth transitions and hover effects
- **Responsive**: Mobile-first responsive design

## 🔐 Authentication Flow

### Authentication Context (`src/contexts/AuthContext.tsx`)

```typescript
interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  register: (userData: RegisterData) => Promise<void>;
}

const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  // Token management
  // User state management
  // API integration
  // Local storage handling
};
```

### Login Flow

1. User submits credentials
2. API call to `/api/users/login`
3. Store JWT token in localStorage
4. Update user context state
5. Redirect to dashboard

### Token Management

- Automatic token inclusion in API requests
- Token expiration handling
- Automatic logout on token expiry
- Refresh token implementation (planned)

## 🚀 Getting Started

### Prerequisites

- Node.js 18 or higher
- npm/yarn/pnpm package manager
- Backend API running on localhost:8080

### Development Setup

1. **Install dependencies**

   ```bash
   npm install
   # or
   yarn install
   # or
   pnpm install
   ```

2. **Environment configuration**

   ```bash
   # Create .env.local file
   echo "NEXT_PUBLIC_API_URL=http://localhost:8080/api" > .env.local
   ```

3. **Run development server**

   ```bash
   npm run dev
   # or
   yarn dev
   # or
   pnpm dev
   ```

4. **Open application**
   ```
   http://localhost:3000
   ```

### Production Build

```bash
# Build the application
npm run build

# Start production server
npm run start

# Or export static files
npm run build && npm run export
```

## 📱 Responsive Design

### Breakpoints

```css
/* Mobile First Approach */
sm: 640px    /* Small screens */
md: 768px    /* Medium screens */
lg: 1024px   /* Large screens */
xl: 1280px   /* Extra large screens */
2xl: 1536px  /* 2X large screens */
```

### Mobile Optimization

- Touch-friendly interface elements
- Optimized navigation for mobile devices
- Responsive images and media
- Mobile-specific UI patterns
- Fast loading with Next.js optimization

## 🧪 Testing

### Component Testing

```bash
# Run component tests (when configured)
npm run test

# Run tests in watch mode
npm run test:watch

# Generate coverage report
npm run test:coverage
```

### E2E Testing (Planned)

- Playwright integration for end-to-end testing
- User flow testing
- Cross-browser compatibility testing

## ⚡ Performance Optimization

### Next.js Features

- **App Router**: Latest Next.js routing system
- **Turbopack**: Fast development builds
- **Image Optimization**: Automatic image optimization
- **Code Splitting**: Automatic route-based code splitting
- **Static Generation**: Pre-rendered pages where possible

### Bundle Optimization

- Tree shaking for unused code elimination
- Dynamic imports for lazy loading
- Optimized dependencies bundling
- Compression and minification

### Loading States

```tsx
// Skeleton loading components
const JobSkeleton = () => (
  <div className="animate-pulse">
    <div className="h-4 bg-gray-300 rounded w-3/4 mb-2"></div>
    <div className="h-4 bg-gray-300 rounded w-1/2"></div>
  </div>
);

// Loading spinners
const LoadingSpinner = () => (
  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
);
```

## 🔧 Configuration Files

### Next.js Config (`next.config.ts`)

```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    turbo: {
      rules: {
        "*.svg": {
          loaders: ["@svgr/webpack"],
          as: "*.js",
        },
      },
    },
  },
  images: {
    domains: ["localhost", "your-backend-domain.com"],
  },
};

export default nextConfig;
```

### TypeScript Config (`tsconfig.json`)

```json
{
  "compilerOptions": {
    "lib": ["dom", "dom.iterable", "es6"],
    "allowJs": true,
    "skipLibCheck": true,
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "module": "esnext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "preserve",
    "incremental": true,
    "plugins": [
      {
        "name": "next"
      }
    ],
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

## 🐛 Troubleshooting

### Common Issues

1. **API Connection Errors**

   ```
   Solution: Verify NEXT_PUBLIC_API_URL in .env.local
   Check if backend server is running on correct port
   ```

2. **Build Errors**

   ```bash
   # Clear Next.js cache
   rm -rf .next
   npm run build
   ```

3. **Styling Issues**

   ```bash
   # Rebuild Tailwind classes
   npm run dev
   # Check if components.json is properly configured
   ```

4. **Authentication Issues**
   ```
   Solution: Clear localStorage and cookies
   Check JWT token expiration
   Verify API endpoints are accessible
   ```

### Development Tools

```bash
# Next.js analyzer for bundle analysis
npm install -g @next/bundle-analyzer
ANALYZE=true npm run build

# TypeScript compiler check
npx tsc --noEmit

# ESLint for code quality
npm run lint
```

## 🚀 Deployment

### Vercel (Recommended)

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy to Vercel
vercel --prod

# Environment variables in Vercel dashboard
NEXT_PUBLIC_API_URL=https://your-backend-api.com/api
```

### Docker Deployment

```dockerfile
FROM node:18-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM base AS build
RUN npm ci
COPY . .
RUN npm run build

FROM base AS runtime
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["npm", "start"]
```

### Static Export

```bash
# For static hosting (GitHub Pages, Netlify, etc.)
npm run build
npm run export
```

## 🤝 Contributing

### Development Guidelines

1. Follow React/Next.js best practices
2. Use TypeScript for type safety
3. Implement responsive design patterns
4. Write meaningful component documentation
5. Test components before submitting PRs

### Code Style

- Use ESLint and Prettier for code formatting
- Follow conventional commit messages
- Use semantic component naming
- Implement proper error boundaries
- Handle loading and error states

## 📚 Resources

- [Next.js Documentation](https://nextjs.org/docs)
- [React Documentation](https://react.dev/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [shadcn/ui Components](https://ui.shadcn.com/)
- [Radix UI Primitives](https://www.radix-ui.com/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

---

Built with ❤️ using Next.js and modern React patterns
