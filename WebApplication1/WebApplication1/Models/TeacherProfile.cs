using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class TeacherProfile
{
    public int Id { get; set; }

    public virtual ICollection<Class> Classes { get; set; } = new List<Class>();

    public virtual ICollection<Event> Events { get; set; } = new List<Event>();

    public virtual User IdNavigation { get; set; } = null!;
}
