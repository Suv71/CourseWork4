using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ServerApp
{
    class Program
    {
        static void Main(string[] args)
        {
            RadioServer server = new RadioServer("10.175.147.229", 6000);
            server.Run();
        }
    }
}
