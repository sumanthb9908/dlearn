# ---------- Stage 1: Build ----------
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

# Install all dependencies (dev + prod needed for build)
RUN npm ci

COPY . .

RUN npm run build

# Install only production dependencies in a clean folder
RUN npm ci --only=production --ignore-scripts && \
    npm cache clean --force

# ---------- Stage 2: Runtime ----------
FROM node:20-alpine

ENV NODE_ENV=production

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only what's needed with correct ownership
COPY --from=builder --chown=appuser:appgroup /app/package*.json ./
COPY --from=builder --chown=appuser:appgroup /app/node_modules ./node_modules
COPY --from=builder --chown=appuser:appgroup /app/dist ./dist

USER appuser

EXPOSE 3000

CMD ["node", "dist/index.js"]
