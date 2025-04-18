using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Notification
{
    public int Id { get; set; }

    public int Userid { get; set; }

    public int Eventid { get; set; }

    public string? NotificationDescription { get; set; }

    public DateTime NotificationTimestamp { get; set; }

    public bool Sent { get; set; }

    public virtual Event? Event { get; set; }

    public virtual User? User { get; set; }
}
