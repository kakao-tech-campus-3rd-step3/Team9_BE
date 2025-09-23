window.onload = () => {
  window.ui = SwaggerUIBundle({
    url: "/api-docs",
    dom_id: '#swagger-ui',
    requestInterceptor: (req) => {
      req.credentials = 'include';
      return req;
    },
  });
};