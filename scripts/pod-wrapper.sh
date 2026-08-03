#!/bin/bash
# Runs CocoaPods using rvm's ruby-3.4.8 instead of Homebrew's Ruby 4.x.
# Homebrew upgraded ruby to 4.0.x which removed bigdecimal from stdlib;
# activesupport (a CocoaPods transitive dep) requires it.
# Requires: rvm use 3.4.8 && gem install cocoapods  (one-time setup)
RVM_RUBY="$HOME/.rvm/rubies/ruby-3.4.8/bin/ruby"
USER_GEM_HOME="$HOME/.gem/ruby/3.4.0"
exec env -i \
  PATH="$HOME/.rvm/bin:/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin" \
  HOME="$HOME" \
  LANG=en_US.UTF-8 \
  GEM_HOME="$USER_GEM_HOME" \
  GEM_PATH="$USER_GEM_HOME:$HOME/.rvm/rubies/ruby-3.4.8/lib/ruby/gems/3.4.0" \
  "$RVM_RUBY" "$USER_GEM_HOME/bin/pod" "$@"
