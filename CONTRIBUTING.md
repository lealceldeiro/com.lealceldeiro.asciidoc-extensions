# Contributing to this repository

## Releasing a new version

- On the `main` branch, do `git pull`
- Create a new chore branch and bump the maven project version -- push the chages
- Open a PR from the newly created branch
- Mege the PR once it's ready
- On the `main` branch, do `git pull` again
- Create a new tag: `git tag release/xxx.yyy.zzz`, where `xxx` is the major version, `yyy` is the minor version, and `zzz` is the patch version. See [semantic versioning](https://semver.org/) for more info.
- Push the new tag `git push --tags`
  
