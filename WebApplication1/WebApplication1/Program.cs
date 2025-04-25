using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Microsoft.OpenApi.Models;
using WebApplication1.Controllers;
using WebApplication1.Dto;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();


builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Basic API for proving stack",
        Version = "v1",
        Description = "A project for Dr. Bilitski's Software engineering class"
    });
    options.EnableAnnotations();
});

// Register DbContext
builder.Services.AddDbContext<GoIn2Context>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

var app = builder.Build();

// Configure the HTTP request pipeline.
// Removed the if (app.Environment.IsDevelopment()) block
app.UseSwagger();
app.UseSwaggerUI();

//app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();