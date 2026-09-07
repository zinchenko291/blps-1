{
  description = "blps";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=26.05";
  };

  outputs =
    { self, nixpkgs }:
    let
      system = "aarch64-darwin";
      pkgs = import nixpkgs {
        inherit system;
        config = { };
        overlays = [ ];
      };
    in
    {
      devShells.${system}.default = pkgs.mkShellNoCC {
        packages = with pkgs; [
          nixd
          nil
          postgresql_16
          jdk21
          maven
        ];

        shellHook = ''
          export JAVA_HOME="${pkgs.jdk21}"
          export PATH="$JAVA_HOME/bin:$PATH"
        '';
      };
    };
}
